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
                        assert (inst.getNextInst() != null);
                        tempOut.addAll(inst.getNextInst().liveIn);
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

    public void run() {
        for(IRFunction irFunction : ir.getFunctionList()) {
            regLivenessAnalyse(irFunction);
        }
    }
}
