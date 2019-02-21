package mxstar.ast;

import mxstar.utility.Location;

public class SubscriptExprNode extends ExprNode {
	private ExprNode array, postfix;

	public SubscriptExprNode(ExprNode array, ExprNode postfix, Location location) {
		this.array = array;
		this.postfix = postfix;
		this.location = location;
	}

	public ExprNode getArray() {
		return array;
	}

	public ExprNode getPostfix() {
		return postfix;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
