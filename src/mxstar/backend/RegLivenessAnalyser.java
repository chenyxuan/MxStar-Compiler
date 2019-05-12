package mxstar.backend;

import mxstar.ir.*;

import java.util.*;

public class RegLivenessAnalyser {
    private IRRoot ir;

    public RegLivenessAnalyser(IRRoot ir) {
        this.ir = ir;
    }

    private void regLivenessAnalyse(IRFunction irFunction) {
        List<BasicBlock> reversedList = new ArrayList<>(irFunction.getAllBB());
        Collections.reverse(reversedList);

        for(BasicBlock block : reversedList) {
            for(IRInstruction inst = block.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                inst.liveIn.clear();
                inst.liveOut.clear();
            }
        }

        Set<VirtualReg> tempIn = new HashSet<>();
        Set<VirtualReg> tempOut = new HashSet<>();

        while(true) {
            boolean flag = false;

            for(BasicBlock block : reversedList) {

                for(IRInstruction inst = block.getTailInst(); inst != null; inst = inst.getPrevInst()) {
                    tempIn.clear();
                    tempOut.clear();

                    if(inst instanceof IRJumpInst) {
                        if(inst instanceof IRJump) {
                            tempOut.addAll(((IRJump) inst).getTargetBB().getHeadInst().liveIn);
                        }
                        else if(inst instanceof IRBranch) {
                            tempOut.addAll(((IRBranch) inst).getThenBB().getHeadInst().liveIn);
                            tempOut.addAll(((IRBranch) inst).getElseBB().getHeadInst().liveIn);
                        }
                    }
                    else {
                        if(inst.getNextInst() != null) {
                            tempOut.addAll(inst.getNextInst().liveIn);
                        }
                    }

                    tempIn.addAll(tempOut);
                    IRRegister definedReg = inst.getDefinedReg();
                    if(definedReg instanceof VirtualReg) {
                        tempIn.remove(definedReg);
                    }
                    for (IRRegister usedReg : inst.getUsedRegisterList()) {
                        if(usedReg instanceof VirtualReg) {
                            tempIn.add((VirtualReg) usedReg);
                        }
                    }

                    if(!inst.liveIn.equals(tempIn)) {
                        flag = true;
                        inst.liveIn.clear();
                        inst.liveIn.addAll(tempIn);
                    }

                    if(!inst.liveOut.equals(tempOut)) {
                        flag = true;
                        inst.liveOut.clear();
                        inst.liveOut.addAll(tempOut);
                    }
                }
            }

            if(!flag) break;
        }
    }

    private boolean tryEliminate(IRFunction irFunction) {
        boolean result = false;

        List<BasicBlock> reversedOrder = irFunction.getAllBB();
        for (BasicBlock bb : reversedOrder) {
            for (IRInstruction inst = bb.getTailInst(), prevInst; inst != null; inst = prevInst) {
                prevInst = inst.getPrevInst();
                if (inst instanceof IRBinaryOp || inst instanceof IRComparison ||
                        inst instanceof IRLoad || inst instanceof IRMove || inst instanceof IRUnaryOp ||
                        inst instanceof IRHeapAlloc) {
                    IRRegister dest = inst.getDefinedReg();
                    VirtualReg keyDest = (VirtualReg) dest;

                    if (dest == null || !inst.liveOut.contains(keyDest)) {
                        result = true;
                        inst.remove();
                    }
                }
            }
        }
        return result;
    }

    private Map<BasicBlock, BasicBlock> jumpTargetBBMap = new HashMap<>();

    private BasicBlock replaceJumpTarget(BasicBlock bb) {
        BasicBlock ret = bb, query = jumpTargetBBMap.get(bb);
        while (query != null) {
            ret = query;
            query = jumpTargetBBMap.get(query);
        }
        return ret;
    }

    private void removeBlankBB(IRFunction func) {
        jumpTargetBBMap.clear();
        for (BasicBlock bb : func.getAllBB()) {
            if (bb.getHeadInst() == bb.getTailInst()) {
                IRInstruction inst = bb.getHeadInst();
                if (inst instanceof IRJump) {
                    jumpTargetBBMap.put(bb, ((IRJump) inst).getTargetBB());
                }
            }
        }
        for (BasicBlock bb : func.getAllBB()) {
            if (bb.getTailInst() instanceof IRJump) {
                IRJump jumpInst = (IRJump) bb.getTailInst();
                jumpInst.setTargetBB(replaceJumpTarget(jumpInst.getTargetBB()));
            } else if (bb.getTailInst() instanceof IRBranch) {
                IRBranch branchInst = (IRBranch) bb.getTailInst();
                branchInst.setThenBB(replaceJumpTarget(branchInst.getThenBB()));
                branchInst.setElseBB(replaceJumpTarget(branchInst.getElseBB()));
                if (branchInst.getThenBB() == branchInst.getElseBB()) {
                    branchInst.replace(new IRJump(bb, branchInst.getThenBB()));
                }
            }
        }
        func.reAllBB();
    }

    private void funkStupidLoop() {

        for(IRRoot.ForRecord record : ir.forRecordList) {
            if(record.removed) continue;

            List<BasicBlock> bbList = new ArrayList<>();

            bbList.add(record.condBB);
            bbList.add(record.stepBB);
            bbList.add(record.bodyBB);
            boolean flag = false;

            IRInstruction nextPartInst = record.afterBB.getHeadInst();

            for(BasicBlock bb : bbList) {
                for(IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                    if(inst instanceof IRFunctionCall && !((IRFunctionCall) inst).getFunc().isOrphan()) {
                        flag = true;
                    }
                    else if (inst.getDefinedReg() != null) {
                        if(inst.getDefinedReg() instanceof StaticData) {
                            flag = true;
                        }
                        else {
                            VirtualReg virtualReg = (VirtualReg) inst.getDefinedReg();
                            if (nextPartInst != null && nextPartInst.liveIn.contains(virtualReg)) {
                                flag = true;
                            }
                        }
                    }
                    else if(inst instanceof IRStore || inst instanceof IRPush || inst instanceof IRReturn) {
                        flag = true;
                    }
                    else if(inst instanceof IRJump) {
                        if((!bbList.contains(((IRJump) inst).getTargetBB()) && ((IRJump) inst).getTargetBB() != record.afterBB)) {
                            flag = true;
                        }
                    }
                    else if(inst instanceof IRBranch) {
                        if((!bbList.contains(((IRBranch) inst).getThenBB()) && ((IRBranch) inst).getThenBB() != record.afterBB)
                                || (!bbList.contains(((IRBranch) inst).getElseBB()) && ((IRBranch) inst).getElseBB() != record.afterBB)) {
                            flag = true;
                        }
                    }
                }
            }

            if(!flag) {
                record.removed = true;
                record.condBB.clear();
                record.condBB.setJumpInst(new IRJump(record.afterBB, record.condBB));
                record.condBB.getFunction().reAllBB();
            }
        }

        for(IRFunction irFunction : ir.getFunctionList()) {
            irFunction.updateCalleeSet();
        }
        ir.updateCalleeSet();
    }

    public void run() {

        for(IRFunction irFunction : ir.getFunctionList()) {
            regLivenessAnalyse(irFunction);
        }
        funkStupidLoop();

        while(true) {
            boolean flag = false;

            for(IRFunction irFunction : ir.getFunctionList()) {
                if(tryEliminate(irFunction)) flag = true;
                removeBlankBB(irFunction);
                regLivenessAnalyse(irFunction);
            }
            funkStupidLoop();

            if(!flag) break;
        }
    }
}
