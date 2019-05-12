package mxstar.backend;

import mxstar.ir.*;
import mxstar.nasm.NASMRegisterSet;
import org.antlr.v4.runtime.misc.Pair;

import java.util.*;

public class RegisterAllocator {
    private IRRoot ir;

    private List<PhysicalReg> physicalRegs;
    private int K;

    private Map<VirtualReg, String> nameMap = new HashMap<>();

    private Set<VirtualReg> precolored = new LinkedHashSet<>();
    private Set<VirtualReg> initial = new LinkedHashSet<>();
    private Set<VirtualReg> simplifyWorklist = new LinkedHashSet<>();
    private Set<VirtualReg> freezeWorklist = new LinkedHashSet<>();
    private Set<VirtualReg> spillWorklist = new LinkedHashSet<>();
    private Set<VirtualReg> spilledNodes = new LinkedHashSet<>();
    private Set<VirtualReg> coalescedNodes = new LinkedHashSet<>();
    private Set<VirtualReg> coloredNodes = new LinkedHashSet<>();
    private Stack<VirtualReg> selectStack = new Stack<>();
    private Set<VirtualReg> selectedNodes = new HashSet<>();

    private Set<IRMove> coalescedMoves = new LinkedHashSet<>();
    private Set<IRMove> constrainedMoves = new LinkedHashSet<>();
    private Set<IRMove> frozenMoves = new LinkedHashSet<>();
    private Set<IRMove> worklistMoves = new LinkedHashSet<>();
    private Set<IRMove> activeMoves = new LinkedHashSet<>();

    private Set<Pair<VirtualReg, VirtualReg> > adjSet = new HashSet<>();
    private Map<VirtualReg, Set<VirtualReg> > adjList = new HashMap<>();
    private Map<VirtualReg, Integer> degree = new HashMap<>();
    private Map<VirtualReg, Set<IRMove> > moveList = new HashMap<>();
    private Map<VirtualReg, VirtualReg> alias = new HashMap<>();


    public RegisterAllocator(IRRoot ir) {
        this.ir = ir;

        physicalRegs = new ArrayList<>(NASMRegisterSet.generalRegs);
        for(IRFunction irFunction : ir.getFunctionList()) {
            ir.updateMaxArgsNum(irFunction.getParaRegs().size());
        }
        if(ir.getMaxArgsNum() >= 5) physicalRegs.remove(NASMRegisterSet.r8);
        if(ir.getMaxArgsNum() >= 6) physicalRegs.remove(NASMRegisterSet.r9);

        PhysicalReg reservedReg = physicalRegs.get(0);
        ir.setReversedRegister(reservedReg);
        physicalRegs.remove(reservedReg);

        K = physicalRegs.size();
    }

    private void clear() {
        precolored.clear();
        initial.clear();
        simplifyWorklist.clear();
        freezeWorklist.clear();
        spillWorklist.clear();
        spilledNodes.clear();
        coalescedNodes.clear();
        coloredNodes.clear();
        selectStack.clear();
        selectedNodes.clear();

        coalescedMoves.clear();
        constrainedMoves.clear();
        frozenMoves.clear();
        worklistMoves.clear();
        activeMoves.clear();

        adjSet.clear();
        adjList.clear();
        degree.clear();
        moveList.clear();
        alias.clear();
    }


    private void init() {
        clear();

        for(BasicBlock b : bbInProgram()) {
            for(IRInstruction I = b.getHeadInst();
                I != null;
                I = I.getNextInst()) {
                for (VirtualReg reg : listUnion(def(I), use(I))) {
                    if (reg.getForcedPhysicalReg() != null) {
                        reg.setColor(reg.getForcedPhysicalReg());
                        precolored.add(reg);
                    } else {
                        initial.add(reg);
                    }
                }
            }
        }
        Set<VirtualReg> allRegs = new HashSet<>();
        allRegs.addAll(precolored);
        allRegs.addAll(initial);

        for (VirtualReg reg : allRegs) {
            adjList.put(reg, new HashSet<>());
            degree.put(reg, 0);
            moveList.put(reg, new HashSet<>());
            alias.put(reg, null);
        }
    }

    static boolean first = true;

    public void run () {
        new RegLivenessAnalyser(ir).run();

        init();
        build();

        System.err.println(initial.size());
        if(initial.size() > 256 && first) {
            new NaiveRegisterAllocator(ir).run();
        }
        else {
            makeWorklist();
            while (true) {
                /*
                System.err.println(simplifyWorklist.size());
                System.err.println(worklistMoves.size());
                System.err.println(freezeWorklist.size());
                System.err.println(spillWorklist.size());
                System.err.println();
                */
                if(!simplifyWorklist.isEmpty()) simplify();
                else if(!worklistMoves.isEmpty()) coalesce();
                else if(!freezeWorklist.isEmpty()) freeze();
                else if(!spillWorklist.isEmpty()) selectSpill();
                else {
                    break;
                }
            }

            System.err.println("assigning");
            assignColor();
            System.err.println("assigned");
            if (!spilledNodes.isEmpty()) {
                rewriteProgram();
                first = false;
                run();
            } else {
                allocate();
            }
        }
    }


    private void build() {
        for(BasicBlock b : bbInProgram()) {
            Set<VirtualReg> live = new HashSet<>(b.getTailInst().liveOut);

            for(IRInstruction I = b.getTailInst(); I != null; I = I.getPrevInst()) {

                if (isMoveInstruction(I)) {
                    live.removeAll(use(I));

                    for (VirtualReg n : listUnion(def(I), use(I))) {
                        moveList.get(n).add((IRMove) I);
                    }
                    worklistMoves.add((IRMove) I);
                }

                live.addAll(def(I));

                for (VirtualReg d : def(I)) {
                    for (VirtualReg l : live) {
                        addEdge(l, d);
                    }
                }

                live.removeAll(def(I));
                live.addAll(use(I));
            }
        }
    }

    private void addEdge(VirtualReg u,VirtualReg v) {
        if(u != v && !adjSet.contains(new Pair<>(u, v))) {
            adjSet.add(new Pair<>(u, v));
            adjSet.add(new Pair<>(v, u));
            if(!precolored.contains(u)) {
                adjList.get(u).add(v);
                degree.put(u, degree.get(u) + 1);
            }
            if(!precolored.contains(v)) {
                adjList.get(v).add(u);
                degree.put(v, degree.get(v) + 1);
            }
        }
    }

    private void makeWorklist() {
        List<VirtualReg> regs = new ArrayList<>(initial);
        for(VirtualReg n : regs) {
            initial.remove(n);
            if(degree.get(n) >= K) {
                spillWorklist.add(n);
            }
            else if(moveRelated(n)) {
                freezeWorklist.add(n);
            }
            else {
                simplifyWorklist.add(n);
            }
        }
    }

    private Set<VirtualReg> adjacent(VirtualReg n) {
        Set<VirtualReg> neighbors = new HashSet<>();
        for (VirtualReg neighbor : adjList.get(n)) {
            if (!selectedNodes.contains(neighbor) && !coalescedNodes.contains(neighbor)) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    private Set<IRMove> nodeMoves(VirtualReg n) {
        Set<IRMove> moves = new HashSet<>();
        for(IRMove move : moveList.get(n)) {
            if(activeMoves.contains(move) || worklistMoves.contains(move)) {
                moves.add(move);
            }
        }
        return moves;
    }

    private boolean moveRelated(VirtualReg n) {
        return !nodeMoves(n).isEmpty();
    }

    private void simplify() {
        VirtualReg n = simplifyWorklist.iterator().next();
        simplifyWorklist.remove(n);
        selectStack.push(n);
        selectedNodes.add(n);
        for(VirtualReg m : adjacent(n)) {
            decrementDegrees(m);
        }
    }

    private void decrementDegrees(VirtualReg m) {
        int d = degree.get(m);
        degree.put(m, d - 1);
        if(d == K) {
            enableMoves(setUnion(Collections.singleton(m), adjacent(m)));
            spillWorklist.remove(m);
            if(moveRelated(m)) {
                freezeWorklist.add(m);
            }
            else {
                simplifyWorklist.add(m);
            }
        }
    }

    private void enableMoves(Set<VirtualReg> nodes) {
        for(VirtualReg n : nodes) {
            for(IRMove m : nodeMoves(n)) {
                if (activeMoves.contains(m)) {
                    activeMoves.remove(m);
                    worklistMoves.remove(m);
                }
            }
        }
    }

    private void addWorkList(VirtualReg u) {
        if(!precolored.contains(u) && !moveRelated(u) && degree.get(u) < K) {
            freezeWorklist.remove(u);
            simplifyWorklist.add(u);
        }
    }

    private boolean ok(VirtualReg t,VirtualReg r) {
        return degree.get(t) < K || precolored.contains(t) || adjSet.contains(new Pair<>(t, r));
    }

    private boolean conservative(Set<VirtualReg> nodes) {
        int k = 0;
        for(VirtualReg n : nodes) {
            if(degree.get(n) >= K) k++;
        }
        return k < K;
    }

    private void combine(VirtualReg u,VirtualReg v) {
        if(freezeWorklist.contains(v)) {
            freezeWorklist.remove(v);
        }
        else {
            spillWorklist.remove(v);
        }
        coalescedNodes.add(v);
//        System.err.println(getName(u) + ' ' + getName(v));
        alias.put(v, u);
        moveList.get(u).addAll(moveList.get(v));
        enableMoves(Collections.singleton(v));

        for(VirtualReg t : adjacent(v)) {
            addEdge(t, u);
            decrementDegrees(t);
        }

        if(degree.get(u) >= K && freezeWorklist.contains(u)) {
            freezeWorklist.remove(u);
            spillWorklist.add(u);
        }
    }

    private VirtualReg getAlias(VirtualReg n) {
        if(coalescedNodes.contains(n))
            return getAlias(alias.get(n));
        else
            return  n;
    }

    private void freeze() {
        VirtualReg u = freezeWorklist.iterator().next();
        freezeWorklist.remove(u);
        simplifyWorklist.add(u);
        freezeMoves(u);
    }



    private void freezeMoves(VirtualReg u) {
        for(IRMove m : nodeMoves(u)) {
            VirtualReg x = (VirtualReg) m.getDest();
            VirtualReg y = (VirtualReg) m.getSrc();

            VirtualReg v;
            if(getAlias(y) == getAlias(u)) {
                v = getAlias(x);
            }
            else {
                v = getAlias(y);
            }
            activeMoves.remove(m);
            frozenMoves.add(m);

            if(freezeWorklist.contains(v) && nodeMoves(v).isEmpty()) {
                freezeWorklist.remove(v);
                simplifyWorklist.add(v);
            }
        }
    }

    private void selectSpill() {
        Iterator<VirtualReg> iterator = spillWorklist.iterator();
        VirtualReg m = iterator.next();
        while(m.isTiny() && iterator.hasNext()) {
            m = iterator.next();
        }
        spillWorklist.remove(m);
        simplifyWorklist.add(m);
        freezeMoves(m);
    }


    private void assignColor() {
        int pos = 0;
        while(!selectStack.isEmpty()) {
            VirtualReg n = selectStack.pop();
            selectedNodes.remove(n);

            Set<PhysicalReg> okColors = new HashSet<>(physicalRegs);
            for(VirtualReg w : adjList.get(n)) {
                if(setUnion(coloredNodes, precolored).contains(getAlias(w))) {
                    if(getAlias(w).getColor() == null) {
                        throw new Error("nwnmn");
                    }
                    okColors.remove(getAlias(w).getColor());
                }
            }
//            System.err.println(getName(n));

            if(okColors.isEmpty()) {
                spilledNodes.add(n);
            }
            else {
                coloredNodes.add(n);
                PhysicalReg c = okColors.iterator().next();
                n.setColor(c);
            }
//            System.err.println(pos++);
        }
        for(VirtualReg n : coalescedNodes) {
            n.setColor(getAlias(n).getColor());
        }
    }

    private boolean isMoveInstruction(IRInstruction inst) {
        return inst instanceof IRMove
                && ((IRMove) inst).getDest() instanceof VirtualReg
                && ((IRMove) inst).getSrc() instanceof VirtualReg;
    }

    private List<VirtualReg> use(IRInstruction inst) {
        List<VirtualReg> list = new ArrayList<>();
        for(IRRegister reg : inst.getUsedRegisterList()) {
            if(reg instanceof VirtualReg) list.add((VirtualReg) reg);
        }
        return list;
    }

    private List<VirtualReg> def(IRInstruction inst) {
        List<VirtualReg> list = new ArrayList<>();
        if(inst.getDefinedReg() != null) {
            if(inst.getDefinedReg() instanceof VirtualReg) list.add((VirtualReg) inst.getDefinedReg());
        }
        return list;
    }

    private List<BasicBlock> bbInProgram() {
        List<BasicBlock> list = new ArrayList<>();
        for (IRFunction irFunction : ir.getFunctionList()) {
            list.addAll(irFunction.getAllBB());
        }
        return list;
    }

    private Set<VirtualReg> listUnion(List<VirtualReg> a,List<VirtualReg> b) {
        Set<VirtualReg> regs = new HashSet<>();
        regs.addAll(a);
        regs.addAll(b);
        return regs;
    }

    private Set<VirtualReg> setUnion(Set<VirtualReg> a,Set<VirtualReg> b) {
        Set<VirtualReg> regs = new HashSet<>();
        regs.addAll(a);
        regs.addAll(b);
        return regs;
    }

    private boolean adjOk(VirtualReg v,VirtualReg u) {
        boolean flag = true;
        for(VirtualReg t : adjacent(v)) {
            if(!ok(t, u)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    private void coalesce() {
        IRMove m = worklistMoves.iterator().next();
        VirtualReg x = getAlias((VirtualReg) m.getDest());
        VirtualReg y = getAlias((VirtualReg) m.getSrc());

        VirtualReg u, v;
        if(precolored.contains(y)) {
            u = y;
            v = x;
        }
        else {
            u = x;
            v = y;
        }

        worklistMoves.remove(m);
        if(u == v) {
            constrainedMoves.add(m);
            addWorkList(u);
        }
        else if(precolored.contains(v) || adjSet.contains(new Pair<>(u, v))) {
            constrainedMoves.add(m);
            addWorkList(u);
            addWorkList(v);
        }
        else if(precolored.contains(u) && adjOk(v, u)
            ||  !precolored.contains(u) && conservative(setUnion(adjacent(u), adjacent(v)))) {
            constrainedMoves.add(m);
            combine(u, v);
            addWorkList(u);
        }
        else {
            activeMoves.add(m);
        }

    }
    private static int cnt = 0;
    private String getName(VirtualReg reg) {
        if(nameMap.get(reg) == null) {
            if(reg.getName() != null) nameMap.put(reg, reg.getName());
            else {
                nameMap.put(reg, "R" + (cnt++));
            }
        }
        return nameMap.get(reg);
    }


    private void rewriteProgram() {
        Map<VirtualReg, StackSlot> rewriteMap = new HashMap<>();

        for(VirtualReg spillNode : spilledNodes) {
            rewriteMap.put(spillNode, new StackSlot("spilled node"));
        }

        for(IRFunction func : ir.getFunctionList()) {
            for(BasicBlock bb : func.getAllBB()) {
                for(IRInstruction inst = bb.getHeadInst();
                    inst != null;
                    inst = inst.getNextInst()) {

                    if(inst instanceof IRFunctionCall) {
                        List<RegValue> args = ((IRFunctionCall) inst).getArgs();
                        for (int i = 0; i < args.size(); ++i) {
                            RegValue reg = args.get(i);
                            if (reg instanceof VirtualReg && spilledNodes.contains(reg)) {
                                args.set(i, rewriteMap.get(reg));
                            }
                        }
                        inst.reloadRegLists();
                    }
                    else {
                        Collection<IRRegister> usedRegisters = inst.getUsedRegisterList();
                        Map<IRRegister, IRRegister> renameMap = new HashMap<>();

                        for (IRRegister reg : usedRegisters) {
                            if (!renameMap.containsKey(reg)) {
                                if (reg instanceof VirtualReg && spilledNodes.contains(reg)) {
                                    renameMap.put(reg, new VirtualReg("new virtual register for spilling"));
                                    inst.prependInst(new IRLoad(renameMap.get(reg), rewriteMap.get(reg), 0, bb));
                                    ((VirtualReg) renameMap.get(reg)).setTiny(true);
                                } else {
                                    renameMap.put(reg, reg);
                                }
                            }
                        }
                        inst.setUsedRegisterList(renameMap);
                    }

                    IRRegister definedRegister = inst.getDefinedReg();
                    if (definedRegister instanceof VirtualReg && spilledNodes.contains(definedRegister)) {
                        inst.setDefinedRegister(new VirtualReg("new virtual register for spilling"));
                        inst.appendInst(new IRStore(rewriteMap.get(definedRegister), 0, inst.getDefinedReg(), bb));
                        ((VirtualReg) inst.getDefinedReg()).setTiny(true);
                    }
                }
            }
        }

    }

    private void allocate() {
        for(IRFunction irFunction : ir.getFunctionList()) {
            for(BasicBlock bb : irFunction.getAllBB()) {
                for(IRInstruction inst = bb.getHeadInst();
                    inst != null;
                    inst = inst.getNextInst()) {

                    if(inst instanceof IRFunctionCall) {
                        List<RegValue> args = ((IRFunctionCall) inst).getArgs();
                        for (int i = 0; i < args.size(); ++i) {
                            RegValue reg = args.get(i);
                            if (reg instanceof VirtualReg) {
                                args.set(i, ((VirtualReg) reg).getColor());
                            }
                        }
                        inst.reloadRegLists();
                    }
                    else {
                        Collection<IRRegister> usedRegisters = inst.getUsedRegisterList();
                        Map<IRRegister, IRRegister> renameMap = new HashMap<>();

                        for (IRRegister reg : usedRegisters) {
                            if (!renameMap.containsKey(reg)) {
                                if (reg instanceof VirtualReg) {
                                    renameMap.put(reg, ((VirtualReg) reg).getColor());
                                }
                                else {
                                    renameMap.put(reg, reg);
                                }
                            }
                        }
                        inst.setUsedRegisterList(renameMap);
                    }

                    IRRegister definedRegister = inst.getDefinedReg();
                    if (definedRegister instanceof VirtualReg) {
                        inst.setDefinedRegister(((VirtualReg) definedRegister).getColor());
                    }
                }
            }
        }

        for(IRFunction irFunction : ir.getFunctionList()) {
            for (BasicBlock bb : irFunction.getAllBB()) {
                for (IRInstruction inst = bb.getHeadInst();
                     inst != null;
                     inst = inst.getNextInst()) {

                    Collection<IRRegister> usedRegisters = inst.getUsedRegisterList();
                    IRRegister definedRegister = inst.getDefinedReg();

                    for(IRRegister reg : usedRegisters) {
                        if (reg instanceof PhysicalReg) {
                            irFunction.usedPhysicalGeneralRegs.add((PhysicalReg) reg);
                        } else if (reg instanceof StackSlot) {
                            if(!irFunction.slotMap.values().contains(reg)
                                && !irFunction.stackSlots.contains(reg)) {
                                irFunction.stackSlots.add((StackSlot) reg);
                            }
                        }
                    }

                    if(definedRegister != null) {
                        if(definedRegister instanceof PhysicalReg) {
                            irFunction.usedPhysicalGeneralRegs.add((PhysicalReg) definedRegister);
                        }
                    }
                }
            }
        }
    }

}
