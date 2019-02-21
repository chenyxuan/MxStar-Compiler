package mxstar.ast;

import mxstar.utility.Location;

public class SuffixExprNode extends ExprNode {
	public enum Ops {
		INC, DEC
	}

	private Ops op;
	private ExprNode expr;

	public SuffixExprNode(Ops op, ExprNode expr, Location location) {
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
