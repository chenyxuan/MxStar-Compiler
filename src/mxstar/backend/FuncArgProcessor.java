package mxstar.backend;

import mxstar.ir.*;
import mxstar.nasm.NASMRegisterSet;

public class FuncArgProcessor {
    private IRRoot ir;

    public FuncArgProcessor(IRRoot ir) {
        this.ir = ir;
    }

    private void funcArgProcess(IRFunction irFunction) {
        BasicBlock beginBB = irFunction.getBeginBB();
//      System.err.println(irFunction.getName());
//      System.err.println(irFunction.getParaRegs().size());
        for(int i = 6; i < irFunction.getParaRegs().size(); i++) {
            VirtualReg argReg = irFunction.getParaRegs().get(i);
            StackSlot argSlot = new StackSlot("arg" + i);
            irFunction.slotMap.put(argReg, argSlot);
            beginBB.getHeadInst().prependInst(new IRLoad(argReg, argSlot, 0, beginBB));
        }

        if(irFunction.getParaRegs().size() > 0) irFunction.getParaRegs().get(0).setForcedPhysicalReg(NASMRegisterSet.rdi);
        if(irFunction.getParaRegs().size() > 1) irFunction.getParaRegs().get(1).setForcedPhysicalReg(NASMRegisterSet.rsi);
        if(irFunction.getParaRegs().size() > 2) irFunction.getParaRegs().get(2).setForcedPhysicalReg(NASMRegisterSet.rdx);
        if(irFunction.getParaRegs().size() > 3) irFunction.getParaRegs().get(3).setForcedPhysicalReg(NASMRegisterSet.rcx);
        if(irFunction.getParaRegs().size() > 4) irFunction.getParaRegs().get(4).setForcedPhysicalReg(NASMRegisterSet.r8);
        if(irFunction.getParaRegs().size() > 5) irFunction.getParaRegs().get(5).setForcedPhysicalReg(NASMRegisterSet.r9);
    }

    public void run() {
        for(IRFunction irFunction : ir.getFunctionList()) {
            funcArgProcess(irFunction);
        }
    }
}
