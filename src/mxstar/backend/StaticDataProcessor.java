package mxstar.backend;

import mxstar.ir.*;

import java.util.*;

public class StaticDataProcessor {
    private IRRoot ir;

    public StaticDataProcessor(IRRoot ir) {
        this.ir = ir;
    }

    private class FuncInfo {
        Set<StaticData> definedStaticData = new HashSet<>();
        Set<StaticData> recursiveDefinedStaticData = new HashSet<>();
        Set<StaticData> recursiveUsedStaticData = new HashSet<>();
        Map<StaticData, VirtualReg> staticDataVirtualRegMap = new HashMap<>();
    }

    private Map<IRFunction, FuncInfo> funcInfoMap = new HashMap<>();

    private VirtualReg getStaticDataVreg(Map<StaticData, VirtualReg> staticDataVregMap, StaticData staticData) {
        VirtualReg vreg = staticDataVregMap.get(staticData);
        if (vreg == null) {
            vreg = new VirtualReg(staticData.getName());
            staticDataVregMap.put(staticData, vreg);
        }
        return vreg;
    }

    public void run() {
        for (IRFunction irFunction : ir.getFunctionList()) {
            FuncInfo funcInfo = new FuncInfo();
            funcInfoMap.put(irFunction, funcInfo);
            for (BasicBlock bb : irFunction.getAllBB()) {
                for (IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {

                    List<IRRegister> usedRegisters = inst.getUsedRegisterList();

                    if (!usedRegisters.isEmpty()) {
                        Map<IRRegister, IRRegister> renameMap = new HashMap<>();

                        for (IRRegister reg : usedRegisters) {
                            if (reg instanceof StaticData && !(reg instanceof StaticStr)) {
                                renameMap.put(reg, getStaticDataVreg(funcInfo.staticDataVirtualRegMap, (StaticData) reg));
                            } else {
                                renameMap.put(reg, reg);
                            }
                        }

                        inst.setUsedRegisterList(renameMap);
                    }

                    IRRegister definedRegister = inst.getDefinedReg();
                    if (definedRegister instanceof StaticData) {
                        VirtualReg staticDataVreg = getStaticDataVreg(funcInfo.staticDataVirtualRegMap, (StaticData) definedRegister);
                        inst.setDefinedRegister(staticDataVreg);
                        funcInfo.definedStaticData.add((StaticData) definedRegister);
                    }
                }
            }

            BasicBlock startBB = irFunction.getBeginBB();
            IRInstruction firstInst = startBB.getHeadInst();
            funcInfo.staticDataVirtualRegMap.forEach((staticData, virtualRegister) ->
                    firstInst.prependInst(new IRLoad(virtualRegister, staticData, 0, startBB)));
        }

        for (IRFunction builtFunc : ir.getBuiltInFunctionList()) {
            funcInfoMap.put(builtFunc, new FuncInfo());
        }

        for (IRFunction irFunction : ir.getFunctionList()) {
            FuncInfo funcInfo = funcInfoMap.get(irFunction);
            funcInfo.recursiveUsedStaticData.addAll(funcInfo.staticDataVirtualRegMap.keySet());
            funcInfo.recursiveDefinedStaticData.addAll(funcInfo.definedStaticData);
            for (IRFunction calleeFunc : irFunction.recursiveCalleeSet) {
                FuncInfo calleeFuncInfo = funcInfoMap.get(calleeFunc);
                funcInfo.recursiveUsedStaticData.addAll(calleeFuncInfo.staticDataVirtualRegMap.keySet());
                funcInfo.recursiveDefinedStaticData.addAll(calleeFuncInfo.definedStaticData);
            }
        }

        for (IRFunction irFunction : ir.getFunctionList()) {
            FuncInfo funcInfo = funcInfoMap.get(irFunction);
            Set<StaticData> usedStaticData = funcInfo.staticDataVirtualRegMap.keySet();
            if (usedStaticData.isEmpty()) continue;
            for (BasicBlock bb : irFunction.getAllBB()) {
                for (IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {
                    if (!(inst instanceof IRFunctionCall)) continue;
                    IRFunction calleeFunc = ((IRFunctionCall) inst).getFunc();
                    FuncInfo calleeFuncInfo = funcInfoMap.get(calleeFunc);

                    for (StaticData staticData : funcInfo.definedStaticData) {
                        if (staticData instanceof StaticStr) continue;
                        if (calleeFuncInfo.recursiveUsedStaticData.contains(staticData)) {
                            inst.prependInst(new IRStore(staticData, 0, funcInfo.staticDataVirtualRegMap.get(staticData), bb));
                        }
                    }

                    if (calleeFuncInfo.recursiveDefinedStaticData.isEmpty()) continue;
                    Set<StaticData> loadStaticDataSet = new HashSet<>(calleeFuncInfo.recursiveDefinedStaticData);
                    loadStaticDataSet.retainAll(usedStaticData);

                    for (StaticData staticData : loadStaticDataSet) {
                        if (staticData instanceof StaticStr) continue;
                        inst.appendInst(new IRLoad(funcInfo.staticDataVirtualRegMap.get(staticData), staticData, 0, bb));
                    }
                }
            }
        }

        for (IRFunction irFunction : ir.getFunctionList()) {
            FuncInfo funcInfo = funcInfoMap.get(irFunction);

            IRReturn retInst = irFunction.getIRReturns().get(0);

            for (StaticData staticData : funcInfo.definedStaticData) {
                retInst.prependInst(new IRStore(staticData, 0, funcInfo.staticDataVirtualRegMap.get(staticData), retInst.getParentBB()));
            }
        }
    }
}
