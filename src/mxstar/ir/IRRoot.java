package mxstar.ir;

import mxstar.scope.FuncEntity;

import java.util.*;

public class IRRoot {
	private Map<String, IRFunction> functionMap = new HashMap<>();
	private List<IRFunction> functionList = new ArrayList<>();
	private List<StaticData> staticDataList = new ArrayList<>();

	public IRRoot() {}

	public void addBuiltInFunctions(List<FuncEntity> funcEntities) {
		for(FuncEntity e : funcEntities) {
			addFunction(new IRFunction(e));
		}
	}

	public void addFunction(IRFunction function) {
		functionMap.put(function.getName(), function);
		functionList.add(function);
	}

	public IRFunction getFunction(String name) {
		if(functionMap.containsKey(name)) return functionMap.get(name);
		return null;
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
}
