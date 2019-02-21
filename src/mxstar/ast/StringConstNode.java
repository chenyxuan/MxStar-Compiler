package mxstar.ast;

import mxstar.utility.Location;

public class StringConstNode extends ConstNode {
	private String value;

	public StringConstNode(String value, Location location) {
		this.value = value;
		this.location = location;
	}

	public String getValue() {
		return value;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
