package mxstar.ast;

import mxstar.utility.Location;

public class MemberAccessExprNode extends EntityExprNode {
	private ExprNode expr;
	private String member;

	public MemberAccessExprNode(ExprNode expr, String member, Location location) {
		this.expr = expr;
		this.member = member;
		this.location = location;
	}

	public ExprNode getExpr() {
		return expr;
	}

	public String getMember() {
		return member;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
