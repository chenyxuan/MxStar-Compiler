package mxstar.ir;

import mxstar.nasm.NASMRegisterSet;
import mxstar.symbol.scope.FuncEntity;

import java.util.*;

public class IRRoot {
	private Map<String, IRFunction> functionMap = new HashMap<>();
	private Map<String, IRFunction> builtInFunctionMap = new HashMap<>();
	private List<IRFunction> functionList = new ArrayList<>();
	private List<IRFunction> builtInFunctionList = new ArrayList<>();
	private List<StaticData> staticDataList = new ArrayList<>();
	private int maxArgsNum = 0;
	private PhysicalReg physicalReg0 = null, physicalReg1 = null;
	public Map<String, StaticStr> staticStrMap = new HashMap<>();

	public IRRoot() {}

	public void addBuiltInFunctions(List<FuncEntity> funcEntities) {
		for(FuncEntity funcEntity : funcEntities) {
			IRFunction irFunction = new IRFunction(funcEntity);
			irFunction.setBuiltIn(true);
			builtInFunctionMap.put(irFunction.getName(), irFunction);
			builtInFunctionList.add(irFunction);
			irFunction.usedPhysicalGeneralRegs.addAll(NASMRegisterSet.generalRegs);
//			System.err.println(irFunction.getName());
		}

		getBuiltInFunction("__string_class__length").setTrivial(true);
		getBuiltInFunction("__array_class__size").setTrivial(true);
	}


	public IRFunction getBuiltInFunction(String name) {
		return builtInFunctionMap.get(name);
	}

	public List<IRFunction> getBuiltInFunctionList() {
		return builtInFunctionList;
	}

	public void addFunction(IRFunction function) {
		functionMap.put(function.getName(), function);
		functionList.add(function);
	}

	public IRFunction getFunction(String name) {
		return functionMap.get(name);
	}

	public List<StaticData> getStaticDataList() {
		return staticDataList;
	}

	public List<IRFunction> getFunctionList() {
		return functionList;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

	public void updateMaxArgsNum(int x) {
		if(x > maxArgsNum) maxArgsNum = x;
	}
	public int getMaxArgsNum() {
		return maxArgsNum;
	}

	public void setPhysicalReg0(PhysicalReg physicalReg0) {
		this.physicalReg0 = physicalReg0;
	}

	public void setPhysicalReg1(PhysicalReg physicalReg1) {
		this.physicalReg1 = physicalReg1;
	}

	public PhysicalReg getPhysicalReg0() {
		return physicalReg0;
	}

	public PhysicalReg getPhysicalReg1() {
		return physicalReg1;
	}

	public void updateCalleeSet() {
		Set<IRFunction> recursiveCalleeSet = new HashSet<>();
		for (IRFunction irFunction : getFunctionList()) {
			irFunction.recursiveCalleeSet.clear();
		}
		boolean changed = true;
		while (changed) {
			changed = false;
			for (IRFunction irFunction : getFunctionList()) {
				recursiveCalleeSet.clear();
				recursiveCalleeSet.addAll(irFunction.calleeSet);
				for (IRFunction calleeFunction : irFunction.calleeSet) {
					recursiveCalleeSet.addAll(calleeFunction.recursiveCalleeSet);
				}
				if (!recursiveCalleeSet.equals(irFunction.recursiveCalleeSet)) {
					irFunction.recursiveCalleeSet.clear();
					irFunction.recursiveCalleeSet.addAll(recursiveCalleeSet);
					changed = true;
				}
			}
		}
	}


}
