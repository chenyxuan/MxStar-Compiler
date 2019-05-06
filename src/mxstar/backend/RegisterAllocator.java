package mxstar.backend;

import mxstar.ir.*;
import mxstar.nasm.NASMRegisterSet;
import mxstar.utility.error.CompilationError;

import java.util.*;

public class RegisterAllocator {
    private IRRoot ir;

    private class VirtualRegInfo {
        Set<VirtualReg> neighbours = new HashSet<>();
        boolean removed = false;
        IRRegister color = null;
        int degree = 0;
        Set<VirtualReg> sameRegs = new HashSet<>();
    }

    private Map<VirtualReg, VirtualRegInfo> infoMap = new HashMap<>();
    private List<VirtualReg> orderedRegs = new ArrayList<>();
    private Set<PhysicalReg> usedColors = new HashSet<>();
    private Set<VirtualReg> nodes = new HashSet<>();
    private Set<VirtualReg> smallNodes = new HashSet<>();

    private Map<IRRegister, IRRegister> renameMap = new HashMap<>();

    private List<PhysicalReg> physicalRegs;
    private PhysicalReg physicalReg0, physicalReg1;
    private int numColors;

    public RegisterAllocator(IRRoot ir) {
        this.ir = ir;

        physicalRegs = new ArrayList<>(NASMRegisterSet.generalRegs);
        for(IRFunction irFunction : ir.getFunctionList()) {
            ir.updateMaxArgsNum(irFunction.getParaRegs().size());
        }
        if(ir.getMaxArgsNum() >= 5) physicalRegs.remove(NASMRegisterSet.r8);
        if(ir.getMaxArgsNum() >= 6) physicalRegs.remove(NASMRegisterSet.r9);

        physicalReg0 = physicalRegs.get(0);
        physicalReg1 = physicalRegs.get(1);
        ir.setPhysicalReg0(physicalReg0);
        ir.setPhysicalReg1(physicalReg1);
        physicalRegs.remove(physicalReg0);
        physicalRegs.remove(physicalReg1);

        numColors = physicalRegs.size();
    }
    private VirtualRegInfo getInfo(VirtualReg reg) {
        VirtualRegInfo res = infoMap.get(reg);

        if(res == null) {
            res = new VirtualRegInfo();
            infoMap.put(reg, res);
        }

        return res;
    }
    private void removeNode(VirtualReg reg) {
        VirtualRegInfo info = getInfo(reg);
        info.removed = true;
        nodes.remove(reg);

        for(VirtualReg neighbour : info.neighbours) {
            VirtualRegInfo neighbourInfo = getInfo(neighbour);
            if(neighbourInfo.removed) continue;
            --neighbourInfo.degree;
            if(neighbourInfo.degree < numColors) {
                smallNodes.add(neighbour);
            }
        }
    }

    private void addEdge(VirtualReg x, VirtualReg y) {
        getInfo(x).neighbours.add(y);
        getInfo(y).neighbours.add(x);
    }

    private void allocate(IRFunction irFunction) {
        infoMap.clear();
        nodes.clear();
        smallNodes.clear();

        for (BasicBlock bb : irFunction.getAllBB()) {
            for (IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                IRRegister definedReg = inst.getDefinedReg();
                if (!(definedReg instanceof VirtualReg)) continue;
                VirtualRegInfo info = getInfo((VirtualReg) definedReg);
                if (inst instanceof IRMove) {
                    RegValue rhs = ((IRMove) inst).getSrc();
                    if (rhs instanceof VirtualReg) {
                        info.sameRegs.add((VirtualReg) rhs);
                        getInfo((VirtualReg) rhs).sameRegs.add((VirtualReg) definedReg);
                    }
                    for (VirtualReg reg : inst.liveOut) {
                        if (reg != rhs && reg != definedReg) {
                            addEdge(reg, (VirtualReg) definedReg);
                        }
                    }
                } else {
                    for (VirtualReg reg : inst.liveOut) {
                        if (reg != definedReg) {
                            addEdge(reg, (VirtualReg) definedReg);
                        }
                    }
                }

            }
        }

        for (VirtualRegInfo info : infoMap.values()) {
            info.degree = info.neighbours.size();
        }

        nodes.addAll(infoMap.keySet());
        for (VirtualReg reg : nodes) {
            if (getInfo(reg).degree < numColors) {
                smallNodes.add(reg);
            }
        }


        orderedRegs.clear();
        while (!nodes.isEmpty()) {
            Iterator<VirtualReg> iterator;
            VirtualReg reg;

            while (!smallNodes.isEmpty()) {
                iterator = smallNodes.iterator();
                reg = iterator.next();
                iterator.remove();
                orderedRegs.add(reg);
            }

            if (nodes.isEmpty()) break;

            iterator = nodes.iterator();
            reg = iterator.next();
            iterator.remove();
            removeNode(reg);
            orderedRegs.add(reg);
        }

        Collections.reverse(orderedRegs);

        for (VirtualReg reg : orderedRegs) {
            VirtualRegInfo info = getInfo(reg);
            info.removed = false;
            usedColors.clear();
            for (VirtualReg neighbour : info.neighbours) {
                VirtualRegInfo neighbourInfo = getInfo(neighbour);
                if (!neighbourInfo.removed && neighbourInfo.color instanceof PhysicalReg) {
                    usedColors.add((PhysicalReg) neighbourInfo.color);
                }
            }
            PhysicalReg chosenPhysicalReg = reg.getPhysicalReg();
            if (chosenPhysicalReg != null) {
                if (usedColors.contains(chosenPhysicalReg)) {
                    throw new Error("WTF???");
                }
                info.color = chosenPhysicalReg;
            } else {
                for (VirtualReg sameReg : info.sameRegs) {
                    IRRegister color = getInfo(sameReg).color;
                    if (color instanceof PhysicalReg && !usedColors.contains(color)) {
                        info.color = color;
                        break;
                    }
                }

                if (info.color == null) {
                    for (PhysicalReg physicalReg : physicalRegs) {
                        if (!usedColors.contains(physicalReg)) {
                            info.color = physicalReg;
                            break;
                        }
                    }
                }

                if (info.color == null) {
                    info.color = irFunction.slotMap.get(reg);
                }

                if (info.color == null) {
                    StackSlot stackSlot = new StackSlot(reg.getName());
                    info.color = stackSlot;
                    irFunction.stackSlots.add(stackSlot);
                }
            }
        }
    }

    private void updateInst(IRFunction func) {
        for (BasicBlock bb : func.getAllBB()) {
            for (IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {

                if (inst instanceof IRFunctionCall) {
                    List<RegValue> args = ((IRFunctionCall) inst).getArgs();
                    for (int i = 0; i < args.size(); ++i) {
                        RegValue reg = args.get(i);
                        if (reg instanceof VirtualReg) {
                            args.set(i, getInfo((VirtualReg) reg).color);
                        }
                    }
                } else {
                    Collection<IRRegister> usedRegisters = inst.getUsedRegisterList();
                    if (!usedRegisters.isEmpty()) {
                        boolean flag = false;
                        renameMap.clear();
                        for (IRRegister reg : usedRegisters) {
                            if (reg instanceof VirtualReg) {
                                IRRegister color = getInfo((VirtualReg) reg).color;
                                if (color instanceof StackSlot) {
                                    PhysicalReg curReg;
                                    if (flag) {
                                        curReg = physicalReg1;
                                    } else {
                                        curReg = physicalReg0;
                                        flag = true;
                                    }
                                    inst.prependInst(new IRLoad(curReg, color, 0,bb));
                                    renameMap.put(reg, curReg);
                                    func.usedPhysicalGeneralRegs.add(curReg);
                                } else {
                                    renameMap.put(reg, color);
                                    func.usedPhysicalGeneralRegs.add((PhysicalReg) color);
                                }
                            } else {
                                renameMap.put(reg, reg);
                            }
                        }
                        inst.setUsedRegisterList(renameMap);
                    }
                }

                IRRegister definedReg = inst.getDefinedReg();
                if (definedReg instanceof VirtualReg) {
                    IRRegister color = getInfo((VirtualReg) definedReg).color;
                    if (color instanceof StackSlot) {
                        inst.setDefinedRegister(physicalReg0);
                        inst.appendInst(new IRStore(color, 0, physicalReg0, bb));
                        func.usedPhysicalGeneralRegs.add(physicalReg0);
                        inst = inst.getNextInst();
                    } else {
                        inst.setDefinedRegister(color);
                        func.usedPhysicalGeneralRegs.add((PhysicalReg) color);
                    }
                }
            }
        }
    }

    public void run () {
        for(IRFunction irFunction : ir.getFunctionList()) {
            allocate(irFunction);
            updateInst(irFunction);
        }
    }
}
