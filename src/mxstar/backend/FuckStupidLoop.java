package mxstar.backend;

import mxstar.ir.*;

import java.util.ArrayList;
import java.util.List;

public class FuckStupidLoop {
    private IRRoot ir;

    public FuckStupidLoop(IRRoot ir) {
        this.ir = ir;
    }

    public void run() {
        for(IRRoot.ForRecord record : ir.forRecordList) {
            List<BasicBlock> bbList = new ArrayList<>();

            bbList.add(record.condBB);
            bbList.add(record.stepBB);
            bbList.add(record.bodyBB);

            boolean flag = false;

            IRInstruction nextPartInst = record.afterBB.getHeadInst();

            for(BasicBlock bb : bbList) {
                for(IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                    if(inst instanceof IRFunctionCall) {
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
                        if(!bbList.contains(((IRJump) inst).getTargetBB())) {
                            flag = true;
                        }
                    }
                    else if(inst instanceof IRBranch) {
                        if(!bbList.contains(((IRBranch) inst).getThenBB())
                            || !bbList.contains(((IRBranch) inst).getElseBB())) {
                            flag = true;
                        }
                    }
                }
            }

            if(!flag) {
                record.condBB.clear();
                record.condBB.setJumpInst(new IRJump(record.afterBB, record.condBB));
            }
        }
    }
}
