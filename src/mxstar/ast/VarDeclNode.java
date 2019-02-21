package mxstar.ast;

import mxstar.scope.VarEntity;
import mxstar.utility.Location;

public class VarDeclNode extends DeclNode{
	private TypeNode type;
	private ExprNode initVal;

	private VarEntity varEntity = null;

	public VarDeclNode(TypeNode type, String name, ExprNode initVal, Location location) {
		this.type = type;
		this.name = name;
		this.initVal = initVal;
		this.location = location;
	}

	public void setVarEntity(VarEntity varEntity) {
		this.varEntity = varEntity;
	}

	public VarEntity getVarEntity() {
		return varEntity;
	}

	public TypeNode getType() {
		return type;
	}

	public ExprNode getInitVal() {
		return initVal;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
