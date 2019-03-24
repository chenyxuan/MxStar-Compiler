package mxstar.backend;

import mxstar.ir.*;
public class BinaryOpProcessor {

    private IRRoot ir;

    public BinaryOpProcessor(IRRoot ir) {
        this.ir = ir;
    }


    private void binaryOpProcess(IRFunction irFunction) {
        for (BasicBlock basicBlock : irFunction.getAllBB()) {
            for(IRInstruction inst = basicBlock.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                if (inst instanceof IRBinaryOp) {
                    IRBinaryOp binaryInst = (IRBinaryOp) inst;

                    if (binaryInst.getDest() == binaryInst.getLhs()) continue;
                    if (binaryInst.getDest() == binaryInst.getRhs()) {
                        if (binaryInst.isCommutativeOp()) {
                            binaryInst.setRhs(binaryInst.getLhs());
                            binaryInst.setLhs(binaryInst.getDest());
                        }
                    }
                }
            }
        }
    }

    public void run() {
        for(IRFunction irFunction : ir.getFunctionList()) {
            binaryOpProcess(irFunction);
        }
    }
}
