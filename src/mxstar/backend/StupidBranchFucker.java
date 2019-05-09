package mxstar.backend;

import mxstar.ir.*;

public class StupidBranchFucker {
    private IRRoot ir;

    public StupidBranchFucker(IRRoot ir) {
        this.ir = ir;
    }

    public void run() {
        for(IRFunction irFunction : ir.getFunctionList()) {
            for(BasicBlock bb : irFunction.getAllBB()) {
                if(bb.getTailInst() instanceof IRBranch
                    && ((IRBranch) bb.getTailInst()).getCond() instanceof IntImm) {
                    BasicBlock targetBB =
                            ((IntImm) ((IRBranch) bb.getTailInst()).getCond()).getValue() != 0
                                    ? ((IRBranch) bb.getTailInst()).getThenBB()
                                    : ((IRBranch) bb.getTailInst()).getElseBB();

                    bb.removeJumpInst();
                    bb.setJumpInst(new IRJump(targetBB, bb));
                }
            }
            irFunction.reAllBB();
        }
    }
}
