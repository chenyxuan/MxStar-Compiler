package mxstar.ast;

import mxstar.utility.Location;

import java.util.List;

public class NewExprNode extends ExprNode {
	private TypeNode newType;
	private List<ExprNode> dims;
	private int classSize = 0;

	public NewExprNode(TypeNode newType, List<ExprNode> dims, Location location) {
		this.newType = newType;
		this.dims = dims;
		this.location = location;
	}

	public TypeNode getNewType() {
		return newType;
	}

	public List<ExprNode> getDims() {
		return dims;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}

	public void setClassSize(int classSize) {
		this.classSize = classSize;
	}

	public int getClassSize() {
		return classSize;
	}
}
