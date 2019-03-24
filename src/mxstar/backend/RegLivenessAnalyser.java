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

        boolean consensus = false;

        do {
            consensus = true;
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
                }
            }

        }while(consensus == false);
    }

    public void run() {
        for(IRFunction irFunction : ir.getFunctionList()) {
            regLivenessAnalyse(irFunction);
        }
    }
}
