package mxstar.ast;


import mxstar.utility.Location;

public class PrefixExprNode extends ExprNode {
	public enum Ops {
		INC, DEC, POS, NEG, LOG_NOT, BIT_NOT
	}

	private Ops op;
	private ExprNode expr;

	public PrefixExprNode(Ops op, ExprNode expr, Location location) {
		this.op = op;
		this.expr = expr;
		this.location = location;
	}

	public Ops getOp() { return op; }

	public ExprNode getExpr() { return expr; }

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
