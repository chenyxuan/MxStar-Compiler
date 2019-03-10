package mxstar.backend;

import mxstar.ir.*;

public class FuncArgProcessor {
    private IRRoot ir;

    public FuncArgProcessor(IRRoot ir) {
        this.ir = ir;
    }

    private void funcArgProcess(IRFunction irFunction) {
        BasicBlock beginBB = irFunction.getBeginBB();
        for(int i = 0; i < irFunction.getParaRegs().size(); i++) {
            VirtualReg argReg = irFunction.getParaRegs().get(i);
            StackSlot argSlot = new StackSlot("arg" + i);
            irFunction.addSlot(argReg, argSlot);
            beginBB.prependInst(beginBB.getHeadInst(), new IRLoad(argReg, argSlot, 0, beginBB));
        }
    }

    public void run() {
        for(IRFunction irFunction : ir.getFunctionList()) {
            funcArgProcess(irFunction);
        }
    }
}
