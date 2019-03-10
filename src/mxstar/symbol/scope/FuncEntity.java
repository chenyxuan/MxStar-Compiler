package mxstar.symbol.scope;

import mxstar.ast.*;
import mxstar.symbol.type.*;

import java.util.ArrayList;
import java.util.List;
import static mxstar.utility.GlobalSymbols.*;

public class FuncEntity extends Entity {
	private Type returnType;
	private List<VarEntity> parameters;
	private ClassEntity classEntity;
	private VarEntity thisEntity = null;

	public FuncEntity(Type returnType, String name, List<VarEntity> parameters, ClassEntity classEntity) {
		super(new FunctionType(name), name);
		this.returnType = returnType;
		this.parameters = parameters;
		this.classEntity = classEntity;
	}
	public FuncEntity(FuncDefNode node, ClassEntity classEntity) {
		super(new FunctionType(node.getName()), node.getName());

		Type returnType = null;
		List<VarEntity> parameters = new ArrayList<>();

		if(node.getReturnType() != null) {
			returnType = node.getReturnType().getType();
		}

		if(classEntity != null) {
			parameters.add(new VarEntity(classEntity.getType(), THIS_NAME));
		}
		for(VarDeclNode e : node.getParameterList()) {
			parameters.add(new VarEntity(e));
		}

		this.returnType = returnType;
		this.parameters = parameters;
		this.classEntity = classEntity;
	}


	public Type getReturnType() {
		return returnType;
	}

	public List<VarEntity> getParameters() {
		return parameters;
	}

	public ClassEntity getClassEntity() {
		return classEntity;
	}

	public boolean isConstruct() {
		return returnType == null;
	}

	public boolean isMember() { return classEntity != null; }

	public void setThisEntity(VarEntity thisEntity) {
		this.thisEntity = thisEntity;
	}

	public VarEntity getThisEntity() {
		return thisEntity;
	}
}
