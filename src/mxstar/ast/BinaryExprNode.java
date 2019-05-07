package mxstar.ast;

import mxstar.utility.Location;

public class BinaryExprNode extends ExprNode {
	public enum Ops {
		MUL, DIV, MOD,
		ADD, SUB, SHL, SHR,
		GT, LT, GEQ, LEQ, EQ, NEQ,
		BIT_AND, BIT_OR, BIT_XOR, LOG_AND, LOG_OR
	}

	private Ops op;
	private ExprNode lhs, rhs;

	public BinaryExprNode(Ops op, ExprNode lhs, ExprNode rhs, Location location) {
		this.op = op;
		this.lhs = lhs;
		this.rhs = rhs;
		this.location = location;
	}

	public Ops getOp() { return op; }

	public ExprNode getLhs() { return lhs; }

	public ExprNode getRhs() { return rhs; }

	public void setOp(Ops op) {
		this.op = op;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}

}
