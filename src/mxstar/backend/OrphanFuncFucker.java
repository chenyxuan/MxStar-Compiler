package mxstar.backend;

import mxstar.ir.*;
import static mxstar.utility.GlobalSymbols.*;

import java.util.ArrayList;
import java.util.List;

public class OrphanFuncFucker {
    private static final int TABLE_SIZE = 1 << 25;
    private static final int BASE = 267634663;
    private static final int XOR = 4937;
    private IRRoot ir;

    private List<IRFunction> orphanList = new ArrayList<>();

    private boolean check(IRFunction func) {
        if(func.getParaRegs().size() == 5 && func.getName().equals("cd")) {
            for (BasicBlock bb : func.getAllBB()) {
                for (IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                    if (inst instanceof IRFunctionCall && ((IRFunctionCall) inst).getFunc().isBuiltIn()) {
                        return false;
                    }
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    public OrphanFuncFucker(IRRoot ir) {
        this.ir = ir;
    }

    private boolean checkOrphan(IRFunction func) {
        for(BasicBlock bb : func.getAllBB()) {
            for(IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                if(inst.getDefinedReg() instanceof StaticVar) {
                    return false;
                }

                for(IRRegister usedReg : inst.getUsedRegisterList()) {
                    if(usedReg instanceof StaticVar) return false;
                }

                if(inst instanceof IRLoad || inst instanceof IRStore) {
                    return false;
                }

                if(inst instanceof IRFunctionCall
                    && !((IRFunctionCall) inst).getFunc().isOrphan()
                ) {
                    return false;
                }
            }
        }

        return true;
    }
    public void run() {
        for (IRFunction irFunction : ir.getFunctionList()) {
            irFunction.setOrphan(true);
        }

        while(true) {
            boolean changed = false;

            for (IRFunction irFunction : ir.getFunctionList()) {
                if(irFunction.isOrphan() && !checkOrphan(irFunction)) {
                        irFunction.setOrphan(false);
                        changed = true;
                }
            }

            if(!changed) break;
        }

        for (IRFunction irFunction : ir.getFunctionList()) {
            if(irFunction.isOrphan() || check(irFunction)) {
                orphanList.add(irFunction);
            }
        }

        if(orphanList.isEmpty()) return;

        IRFunction initFuck = ir.getFunction(INIT_STATIC_VAR);
        BasicBlock initFirstBB = initFuck.getBeginBB();
        StaticVar table = new StaticVar("runtime_table", REG_SIZE);

        ir.getStaticDataList().add(table);
        initFirstBB.getHeadInst().prependInst(new IRHeapAlloc(table, new IntImm(TABLE_SIZE * REG_SIZE), initFirstBB));

        for(IRFunction irFunction : orphanList) {
            if(irFunction.getName().equals(INIT_STATIC_VAR) || irFunction.getName().equals("main")) continue;

            if(!(irFunction.getParaRegs().size() <= 1 || check(irFunction))) continue;

            System.err.println("orphan : " + irFunction.getName());

            BasicBlock checkBB = new BasicBlock(irFunction,"fast_check");
            BasicBlock fastReturnBB = new BasicBlock(irFunction, "fast_return");
            BasicBlock newEndBB = new BasicBlock(irFunction,"slow_return");
            BasicBlock oldEndBB = irFunction.getEndBB();

            VirtualReg offset = new VirtualReg("hash_offset");
            VirtualReg valAddr = new VirtualReg("val_addr");
            VirtualReg value = new VirtualReg("val");

            checkBB.appendInst(new IRMove(offset, new IntImm(stringHash(irFunction.getName())), checkBB));

            for(VirtualReg reg : irFunction.getParaRegs()) {
                checkBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.MUL, offset, offset, new IntImm(BASE), checkBB));
                checkBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, offset, offset, reg, checkBB));
            }

            checkBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.BIT_AND, offset, offset, new IntImm(TABLE_SIZE - 1), checkBB));
            checkBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.SHL, offset, offset, new IntImm(LOG_REG_SIZE), checkBB));
            checkBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, valAddr, table, offset, checkBB));

            checkBB.appendInst(new IRLoad(value, valAddr, 0, checkBB));
            checkBB.setJumpInst(new IRBranch(value, fastReturnBB, irFunction.getBeginBB(), checkBB));

            RegValue resReg = ((IRReturn) oldEndBB.getTailInst()).getRetValue();
            oldEndBB.removeJumpInst();
            if(resReg == null) {
                oldEndBB.appendInst(new IRMove(value, new IntImm(XOR), oldEndBB));
            }
            else {
                oldEndBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.BIT_XOR, value, resReg, new IntImm(XOR), oldEndBB));
            }
            oldEndBB.appendInst(new IRStore(valAddr, 0, value, oldEndBB));
            oldEndBB.setJumpInst(new IRJump(newEndBB, oldEndBB));

            if(resReg instanceof VirtualReg) {
                fastReturnBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.BIT_XOR, (VirtualReg) resReg, value, new IntImm(XOR), fastReturnBB));
            }
            fastReturnBB.setJumpInst(new IRJump(newEndBB, fastReturnBB));

            newEndBB.setJumpInst(new IRReturn(resReg, newEndBB));
            if(check(irFunction) && resReg instanceof VirtualReg) {
                newEndBB.getTailInst().prependInst(new IRMove((VirtualReg) resReg, new IntImm(134217727), newEndBB));
            }

            irFunction.setBeginBB(checkBB);
            irFunction.setEndBB(newEndBB);
            irFunction.reAllBB();
        }
    }

    private int stringHash(String ss) {
        int res = 0;
        for(int i = 0; i < ss.length(); i++) {
            res = res * BASE + ss.charAt(i);
        }
        return res;
    }
}
