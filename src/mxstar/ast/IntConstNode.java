package mxstar.ast;

import mxstar.utility.Location;

public class IntConstNode extends ConstNode {
	private int value;

	public IntConstNode(int value, Location location) {
		this.value = value;
		this.location = location;
	}

	public int getValue() {
		return value;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
