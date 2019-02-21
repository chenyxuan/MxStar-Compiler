package mxstar.ast;

import mxstar.utility.Location;

public class BoolConstNode extends ConstNode {
	private Boolean value;

	public BoolConstNode(Boolean value, Location location) {
		this.value = value;
		this.location = location;
	}

	public Boolean getValue() {
		return value;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
