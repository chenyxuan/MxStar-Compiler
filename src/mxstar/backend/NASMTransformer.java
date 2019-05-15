package mxstar.backend;

import mxstar.ir.*;
import mxstar.nasm.NASMRegister;

import static mxstar.utility.GlobalSymbols.*;
import java.util.*;

import static mxstar.nasm.NASMRegisterSet.*;

public class NASMTransformer {
    private IRRoot ir;

    public NASMTransformer(IRRoot ir) {
        this.ir = ir;
    }

    private class FuncInfo {
        List<PhysicalReg> usedCallerSaveRegs = new ArrayList<>();
        List<PhysicalReg> usedCalleeSaveRegs = new ArrayList<>();
        Set<PhysicalReg> recursiveUsedRegs = new HashSet<>();
        Map<StackSlot, Integer> stackSlotOffsetMap = new HashMap<>();
        int numExtraArgs, numStackSlot = 0;
    }

    private Map<IRFunction, FuncInfo> funcInfoMap = new HashMap<>();

    public void run() {
        for (IRFunction irFunction : ir.getFunctionList()) {
            FuncInfo funcInfo = new FuncInfo();
            for (PhysicalReg preg : irFunction.usedPhysicalGeneralRegs) {
                if (preg.isCalleeSave()) funcInfo.usedCalleeSaveRegs.add(preg);
                if (preg.isCallerSave()) funcInfo.usedCallerSaveRegs.add(preg);
            }
            funcInfo.usedCalleeSaveRegs.add(rbx);
            funcInfo.usedCalleeSaveRegs.add(rbp);

            funcInfo.numStackSlot = irFunction.stackSlots.size();
            for (int i = 0; i < funcInfo.numStackSlot; ++i) {
                funcInfo.stackSlotOffsetMap.put(irFunction.stackSlots.get(i), i * REG_SIZE);
            }
            if ((funcInfo.usedCalleeSaveRegs.size() + funcInfo.numStackSlot) % 2 == 0) {
                ++funcInfo.numStackSlot;
            }

            funcInfo.numExtraArgs = irFunction.getParaRegs().size() - 6;
            if (funcInfo.numExtraArgs < 0) funcInfo.numExtraArgs = 0;

            int extraArgOffset = (funcInfo.usedCalleeSaveRegs.size() + funcInfo.numStackSlot + 1) * REG_SIZE; // return address
            for (int i = 6; i < irFunction.getParaRegs().size(); ++i) {
                funcInfo.stackSlotOffsetMap.put(irFunction.slotMap.get(irFunction.getParaRegs().get(i)), extraArgOffset);
                extraArgOffset += REG_SIZE;
            }
            funcInfoMap.put(irFunction, funcInfo);
        }

        for (IRFunction builtinFunc : ir.getBuiltInFunctionList()) {
            funcInfoMap.put(builtinFunc, new FuncInfo());
        }

        for (IRFunction irFunction : funcInfoMap.keySet()) {
            FuncInfo funcInfo = funcInfoMap.get(irFunction);
            funcInfo.recursiveUsedRegs.addAll(irFunction.usedPhysicalGeneralRegs);
            for (IRFunction calleeFunc : irFunction.recursiveCalleeSet) {
                funcInfo.recursiveUsedRegs.addAll(calleeFunc.usedPhysicalGeneralRegs);
            }
        }

        for (IRFunction irFunction : ir.getFunctionList()) {
            FuncInfo funcInfo = funcInfoMap.get(irFunction);

            BasicBlock entryBB = irFunction.getBeginBB();
            IRInstruction firstInst = entryBB.getHeadInst();
            for (PhysicalReg preg : funcInfo.usedCalleeSaveRegs) {
                firstInst.prependInst(new IRPush(preg, entryBB));
            }
            if (funcInfo.numStackSlot > 0)
                firstInst.prependInst(new IRBinaryOp(IRBinaryOp.Ops.SUB, rsp, rsp, new IntImm(funcInfo.numStackSlot * REG_SIZE), entryBB));
            firstInst.prependInst(new IRMove(rbp, rsp, entryBB));

            for (BasicBlock bb : irFunction.getAllBB()) {
                for (IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                    if (inst instanceof IRFunctionCall) {
                        IRFunction calleeFunc = ((IRFunctionCall) inst).getFunc();
                        FuncInfo calleeInfo = funcInfoMap.get(calleeFunc);

                        int numPushCallerSave = 0;
                        for (PhysicalReg preg : funcInfo.usedCallerSaveRegs) {
                            if (preg.isArg6() && preg.getArg6Idx() < irFunction.getParaRegs().size()) continue;
                            if (calleeInfo.recursiveUsedRegs.contains(preg)) {
                                ++numPushCallerSave;
                                inst.prependInst(new IRPush(preg, inst.getParentBB()));
                            }
                        }
                        
                        int numPushArg6Regs = irFunction.getParaRegs().size() <= 6 ? irFunction.getParaRegs().size() : 6;
                        for (int i = 0; i < numPushArg6Regs; ++i) {
                            inst.prependInst(new IRPush(arg6.get(i), inst.getParentBB()));
                        }
                        numPushCallerSave += numPushArg6Regs;

                        boolean extraPush = false;
                        List<RegValue> args = ((IRFunctionCall) inst).getArgs();
                        List<Integer> arg6BakOffset = new ArrayList<>();
                        Map<PhysicalReg, Integer> arg6BakOffsetMap = new HashMap<>();

                        if ((numPushCallerSave + calleeInfo.numExtraArgs) % 2 == 1) {
                            extraPush = true;
                            inst.prependInst(new IRPush(new IntImm(0), inst.getParentBB()));
                        }
                        for (int i = args.size() - 1; i > 5; --i) {
                            if (args.get(i) instanceof StackSlot) {
                                StackSlot stackSlot = (StackSlot) args.get(i);
                                inst.prependInst(new IRLoad(rax, rbp, funcInfo.stackSlotOffsetMap.get(stackSlot), inst.getParentBB()));
                                inst.prependInst(new IRPush(rax, inst.getParentBB()));
                            } else {
                                inst.prependInst(new IRPush(args.get(i), inst.getParentBB()));
                            }
                        }

                        int bakOffset = 0;
                        for (int i = 0; i < 6; ++i) {
                            if (args.size() <= i) break;
                            if (args.get(i) instanceof PhysicalReg && ((PhysicalReg) args.get(i)).isArg6() && ((PhysicalReg) args.get(i)).getArg6Idx() < args.size()) {
                                PhysicalReg preg = (PhysicalReg) args.get(i);
                                if (arg6BakOffsetMap.containsKey(preg)) {
                                    arg6BakOffset.add(arg6BakOffsetMap.get(preg));
                                } else {
                                    arg6BakOffset.add(bakOffset);
                                    arg6BakOffsetMap.put(preg, bakOffset);
                                    inst.prependInst(new IRPush(preg, inst.getParentBB()));
                                    ++bakOffset;
                                }
                            } else {
                                arg6BakOffset.add(-1);
                            }
                        }

                        for (int i = 0; i < 6; ++i) {
                            if (args.size() <= i) break;
                            if (arg6BakOffset.get(i) == -1) {
                                if (args.get(i) instanceof StackSlot) {
                                    StackSlot stackSlot = (StackSlot) args.get(i);
                                    inst.prependInst(new IRLoad(rax, rbp, funcInfo.stackSlotOffsetMap.get(stackSlot), inst.getParentBB()));
                                    inst.prependInst(new IRMove(arg6.get(i), rax, inst.getParentBB()));
                                } else {
                                    inst.prependInst(new IRMove(arg6.get(i), args.get(i), inst.getParentBB()));
                                }
                            } else {
                                inst.prependInst(new IRLoad(arg6.get(i), rsp, REG_SIZE * (bakOffset - arg6BakOffset.get(i) - 1), inst.getParentBB()));
                            }
                        }

                        if (bakOffset > 0) {
                            inst.prependInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, rsp, rsp, new IntImm(bakOffset * REG_SIZE), inst.getParentBB()));
                        }

                        if (((IRFunctionCall) inst).getDest() != null) {
                            inst.appendInst(new IRMove(((IRFunctionCall) inst).getDest(), rax, inst.getParentBB()));
                        }

                        for (PhysicalReg preg : funcInfo.usedCallerSaveRegs) {
                            if (preg.isArg6() && preg.getArg6Idx() < irFunction.getParaRegs().size()) continue;
                            if (calleeInfo.recursiveUsedRegs.contains(preg)) {
                                inst.appendInst(new IRPop(inst.getParentBB(), preg));
                            }
                        }
                        for (int i = 0; i < numPushArg6Regs; ++i) {
                            inst.appendInst(new IRPop(inst.getParentBB(), arg6.get(i)));
                        }

                        if (calleeInfo.numExtraArgs > 0 || extraPush) {
                            int numPushArg = extraPush ? calleeInfo.numExtraArgs + 1 : calleeInfo.numExtraArgs;
                            inst.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, rsp, rsp, new IntImm(numPushArg * REG_SIZE), inst.getParentBB()));
                        }
                    } else if (inst instanceof IRHeapAlloc) {
                        int numPushCallerSave = 0;
                        for (PhysicalReg preg : funcInfo.usedCallerSaveRegs) {
                            ++numPushCallerSave;
                            inst.prependInst(new IRPush(preg, inst.getParentBB()));
                        }
                        inst.prependInst(new IRMove(rdi, ((IRHeapAlloc) inst).getAllocSize(), inst.getParentBB()));
                        if (numPushCallerSave % 2 == 1) {
                            inst.prependInst(new IRPush(new IntImm(0), inst.getParentBB()));
                        }
                        inst.appendInst(new IRMove(((IRHeapAlloc) inst).getDest(), rax, inst.getParentBB()));
                        for (PhysicalReg preg : funcInfo.usedCallerSaveRegs) {
                            inst.appendInst(new IRPop(inst.getParentBB(), preg));
                        }
                        if (numPushCallerSave % 2 == 1) {
                            inst.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, rsp, rsp, new IntImm(REG_SIZE), inst.getParentBB()));
                        }

                    } else if (inst instanceof IRLoad) {
                        if (((IRLoad) inst).getAddr() instanceof StackSlot) {
                            StackSlot stackSlot = (StackSlot) ((IRLoad) inst).getAddr();
                            ((IRLoad) inst).setOffset(funcInfo.stackSlotOffsetMap.get(stackSlot));
                            ((IRLoad) inst).setAddr(rbp);
                        }
                    } else if (inst instanceof IRStore) {
                        if (((IRStore) inst).getAddr() instanceof StackSlot) {
                            StackSlot stackSlot = (StackSlot) ((IRStore) inst).getAddr();
                            ((IRStore) inst).setOffset(funcInfo.stackSlotOffsetMap.get(stackSlot));
                            ((IRStore) inst).setAddr(rbp);
                        }
                    } else if (inst instanceof IRMove) {
                        if (((IRMove) inst).getSrc() == ((IRMove) inst).getDest()) {
                            inst.remove();
                        }
                    }
                }
            }

            IRReturn retInst = irFunction.getIRReturns().get(0);
            if (retInst.getRetValue() != null) {
                retInst.prependInst(new IRMove(rax, retInst.getRetValue(), retInst.getParentBB()));
            }

            IRInstruction lastInst = irFunction.getIRReturns().get(0);
            if (funcInfo.numStackSlot > 0)
                lastInst.prependInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, rsp, rsp, new IntImm(funcInfo.numStackSlot * REG_SIZE), entryBB));
            for (int i = funcInfo.usedCalleeSaveRegs.size() - 1; i >= 0; --i) {
                lastInst.prependInst(new IRPop(entryBB, funcInfo.usedCalleeSaveRegs.get(i)));
            }
        }
    }
}
