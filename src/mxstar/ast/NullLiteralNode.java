package mxstar.ast;

import mxstar.utility.Location;

public class NullLiteralNode extends ConstNode {
	public NullLiteralNode(Location location) {
		this.location = location;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
