package mxstar.ast;

import mxstar.utility.Location;

public class ThisExprNode extends EntityExprNode {
	public ThisExprNode(Location location) {
		this.location = location;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
