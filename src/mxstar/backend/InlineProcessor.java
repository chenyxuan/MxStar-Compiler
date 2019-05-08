package mxstar.backend;

import mxstar.ir.*;

import java.util.*;

public class InlineProcessor {
    private final static int MAX_INLINE_INST = 30;
    private final static int MAX_LOW_INLINE_INST = 30;
    private final static int MAX_FUNC_INST = 1 << 12;
    private final static int MAX_INLINE_DEPTH = 5;

    private IRRoot ir;

    private class FuncInfo {
        int numInst = 0, numCalled = 0;
        boolean recursiveCall, memFunc = false;
    }

    private Map<IRFunction, FuncInfo> funcInfoMap = new HashMap<>();
    private Map<IRFunction, IRFunction> funcBakUpMap = new HashMap<>();

    public InlineProcessor(IRRoot ir) {
        this.ir = ir;
    }

    public void run() {
        for (IRFunction irFunction : ir.getFunctionList()) {
            irFunction.setRecursiveCall(irFunction.recursiveCalleeSet.contains(irFunction));
            FuncInfo funcInfo = new FuncInfo();
            funcInfo.recursiveCall = irFunction.isRecursiveCall();
            funcInfo.memFunc = irFunction.isMember();
            funcInfoMap.put(irFunction, funcInfo);
        }
        for (IRFunction irFunction : ir.getFunctionList()) {
            FuncInfo funcInfo = funcInfoMap.get(irFunction);

            List<BasicBlock> reversedBBList = new ArrayList<>(irFunction.getAllBB());
            Collections.reverse(reversedBBList);

            for (BasicBlock bb : reversedBBList) {
                for (IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                    ++funcInfo.numInst;
                    if (inst instanceof IRFunctionCall) {
                        FuncInfo calleeInfo = funcInfoMap.get(((IRFunctionCall) inst).getFunc());
                        if (calleeInfo != null) {
                            ++calleeInfo.numCalled;
                        }
                    }
                }
            }
        }

        List<BasicBlock> reversePostOrder = new ArrayList<>();
        List<String> unCalledFuncs = new ArrayList<>();
        boolean changed = true, thisFuncChanged;
        while (changed) {
            changed = false;
            unCalledFuncs.clear();
            for (IRFunction irFunction : ir.getFunctionList()) {
                FuncInfo funcInfo = funcInfoMap.get(irFunction);
                List<BasicBlock> reversedBBList = new ArrayList<>(irFunction.getAllBB());
                Collections.reverse(reversedBBList);
                reversePostOrder.clear();
                reversePostOrder.addAll(reversedBBList);
                thisFuncChanged = false;
                for (BasicBlock bb : reversePostOrder) {
                    for (IRInstruction inst = bb.getHeadInst(), nextInst; inst != null; inst = nextInst) {
                        // inst.getNextInst() may be changed later
                        nextInst = inst.getNextInst();
                        if (!(inst instanceof IRFunctionCall)) continue;
                        FuncInfo calleeInfo = funcInfoMap.get(((IRFunctionCall) inst).getFunc());
                        if (calleeInfo == null) continue; // skip built-in functions
                        if (calleeInfo.recursiveCall) continue; // skip self recursive function
                        if (calleeInfo.memFunc) continue;
                        if (calleeInfo.numInst > MAX_LOW_INLINE_INST || calleeInfo.numInst + funcInfo.numInst > MAX_FUNC_INST) continue;

                        nextInst = inlineFunctionCall((IRFunctionCall) inst);
                        funcInfo.numInst += calleeInfo.numInst;
                        changed = true;
                        thisFuncChanged = true;
                        --calleeInfo.numCalled;
                        if (calleeInfo.numCalled == 0) {
                            unCalledFuncs.add(((IRFunctionCall) inst).getFunc().getName());
                        }
                    }
                }
                if (thisFuncChanged) {
                    irFunction.reAllBB();
                }
            }
            for (String funcName : unCalledFuncs) {
                ir.removeFunc(funcName);
            }
        }
        for (IRFunction irFunction : ir.getFunctionList()) {
            irFunction.updateCalleeSet();
        }
        ir.updateCalleeSet();

        // inline recursive functions
        reversePostOrder = new ArrayList<>();
        changed = true;
        for (int i = 0; changed && i < MAX_INLINE_DEPTH; ++i) {
            changed = false;

            // bak up self recursive functions
            funcBakUpMap.clear();
            for (IRFunction irFunction : ir.getFunctionList()) {
                FuncInfo funcInfo = funcInfoMap.get(irFunction);
                if (!funcInfo.recursiveCall) continue;
                funcBakUpMap.put(irFunction, genBakUpFunc(irFunction));
            }

            for (IRFunction irFunction : ir.getFunctionList()) {
                FuncInfo funcInfo = funcInfoMap.get(irFunction);
                List<BasicBlock> reversedBBList = new ArrayList<>(irFunction.getAllBB());
                Collections.reverse(reversedBBList);

                reversePostOrder.clear();
                reversePostOrder.addAll(reversedBBList);
                thisFuncChanged = false;
                for (BasicBlock bb : reversePostOrder) {
                    for (IRInstruction inst = bb.getHeadInst(), nextInst; inst != null; inst = nextInst) {
                        // inst.getNextInst() may be changed later
                        nextInst = inst.getNextInst();
                        if (!(inst instanceof IRFunctionCall)) continue;
                        FuncInfo calleeInfo = funcInfoMap.get(((IRFunctionCall) inst).getFunc());
                        if (calleeInfo == null) continue; // skip built-in functions
                        if (calleeInfo.memFunc) continue;
                        if (calleeInfo.numInst > MAX_INLINE_INST || calleeInfo.numInst + funcInfo.numInst > MAX_FUNC_INST) continue;

                        nextInst = inlineFunctionCall((IRFunctionCall) inst);
                        int numAddInst = calleeInfo.numInst;
                        funcInfo.numInst += numAddInst;
                        changed = true;
                        thisFuncChanged = true;
                    }
                }
                if (thisFuncChanged) {
                    irFunction.reAllBB();
                }
            }
        }
        for (IRFunction irFunction : ir.getFunctionList()) {
            irFunction.updateCalleeSet();
        }
        ir.updateCalleeSet();
    }

    private IRFunction genBakUpFunc(IRFunction func) {
        IRFunction bakFunc = new IRFunction();
        Map<Object, Object> bbRenameMap = new HashMap<>();
        List<BasicBlock> reversedBBList = new ArrayList<>(func.getAllBB());
        Collections.reverse(reversedBBList);
        for (BasicBlock bb : reversedBBList) {
            bbRenameMap.put(bb, new BasicBlock(bakFunc, bb.getName()));
        }
        for (BasicBlock bb : reversedBBList) {
            BasicBlock bakBB = (BasicBlock) bbRenameMap.get(bb);
            for (IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                if (inst instanceof IRJumpInst) {
                    bakBB.setJumpInst((IRJumpInst) inst.copyRename(bbRenameMap));
                } else {
                    bakBB.appendInst(inst.copyRename(bbRenameMap));
                }
            }
        }

        bakFunc.setBeginBB((BasicBlock) bbRenameMap.get(func.getBeginBB()));
        bakFunc.setEndBB((BasicBlock) bbRenameMap.get(func.getEndBB()));
        bakFunc.setParaRegs(func.getParaRegs());
        return bakFunc;
    }

    private IRInstruction inlineFunctionCall(IRFunctionCall funcCallInst) {
        IRFunction callerFunc = funcCallInst.getParentBB().getFunction(), calleeFunc;
        calleeFunc = funcBakUpMap.getOrDefault(funcCallInst.getFunc(), funcCallInst.getFunc());

        List<BasicBlock> reversedBBList = new ArrayList<>(calleeFunc.getAllBB());
        Collections.reverse(reversedBBList);

        Map<Object, Object> renameMap = new HashMap<>();
        BasicBlock oldEndBB = calleeFunc.getEndBB();

        if(oldEndBB == null) {
            System.err.println(calleeFunc.getName());
            throw new Error("Null Old EndBB");
        }

        BasicBlock newEndBB = new BasicBlock(callerFunc, oldEndBB.getName());
        renameMap.put(oldEndBB, newEndBB);
        renameMap.put(calleeFunc.getBeginBB(), funcCallInst.getParentBB());
        if (callerFunc.getEndBB() == funcCallInst.getParentBB()) {
            callerFunc.setEndBB(newEndBB);
        }

        Map<Object, Object> callBBRenameMap = Collections.singletonMap(funcCallInst.getParentBB(), newEndBB);
        for (IRInstruction inst = funcCallInst.getNextInst(); inst != null; inst = inst.getNextInst()) {
            if (inst instanceof IRJumpInst) {
                newEndBB.setJumpInst((IRJumpInst) (inst.copyRename(callBBRenameMap)));
            } else {
                newEndBB.appendInst(inst.copyRename(callBBRenameMap));
            }
            inst.remove();
        }
        IRInstruction newEndBBFisrtInst = newEndBB.getHeadInst();
        for (int i = 0; i < funcCallInst.getArgs().size(); ++i) {
            VirtualReg oldArgVreg = calleeFunc.getParaRegs().get(i);
            VirtualReg newArgVreg = oldArgVreg.copy();
            funcCallInst.prependInst(new IRMove(newArgVreg, funcCallInst.getArgs().get(i), funcCallInst.getParentBB()));
            renameMap.put(oldArgVreg, newArgVreg);
        }
        funcCallInst.remove();
        for (BasicBlock bb : reversedBBList) {
            if (!renameMap.containsKey(bb)) {
                renameMap.put(bb, new BasicBlock(callerFunc, bb.getName()));
            }
        }
        for (BasicBlock oldBB : reversedBBList) {
            BasicBlock newBB = (BasicBlock) renameMap.get(oldBB);
            if (oldBB.forNode != null) {
                IRRoot.ForRecord forRec = oldBB.forNode.forRecord;
                if (forRec.condBB == oldBB) forRec.condBB = newBB;
                if (forRec.stepBB == oldBB) forRec.stepBB = newBB;
                if (forRec.bodyBB == oldBB) forRec.bodyBB = newBB;
                if (forRec.afterBB == oldBB) forRec.afterBB = newBB;
            }
            for (IRInstruction inst = oldBB.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                for (RegValue usedRegValue : inst.getUsedRegValueList()) {
                    copyRegValue(renameMap, usedRegValue);
                }
                if (inst.getDefinedReg() != null) {
                    copyRegValue(renameMap, inst.getDefinedReg());
                }
                if (newBB == newEndBB) {
                    if (!(inst instanceof IRReturn)) {
                        newEndBBFisrtInst.prependInst(inst.copyRename(renameMap));
                    }
                } else {
                    if (inst instanceof IRJumpInst) {
                        if (!(inst instanceof IRReturn)) {
                            newBB.setJumpInst((IRJumpInst) inst.copyRename(renameMap));
                        }
                    } else {
                        newBB.appendInst(inst.copyRename(renameMap));
                    }
                }
            }
        }
        if (!funcCallInst.getParentBB().isEscaped()) {
            funcCallInst.getParentBB().setJumpInst(new IRJump(newEndBB, funcCallInst.getParentBB()));
        }
        IRReturn returnInst = calleeFunc.getIRReturns().get(0);
        if (returnInst.getRetValue() != null) {
            newEndBBFisrtInst.prependInst(new IRMove(funcCallInst.getDest(), (RegValue) renameMap.get(returnInst.getRetValue()), newEndBB));
        }

        return newEndBB.getHeadInst();
    }

    private void copyRegValue(Map<Object, Object> renameMap, RegValue regValue) {
        if (!renameMap.containsKey(regValue)) {
            renameMap.put(regValue, regValue.copy());
        }
    }
}
