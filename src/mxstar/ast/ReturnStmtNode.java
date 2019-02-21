package mxstar.ast;

import mxstar.utility.Location;

public class ReturnStmtNode extends JumpStmtNode {
	ExprNode expr;

	public ReturnStmtNode(ExprNode expr, Location location) {
		this.expr = expr;
		this.location = location;
	}

	public ExprNode getExpr() {
		return expr;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
