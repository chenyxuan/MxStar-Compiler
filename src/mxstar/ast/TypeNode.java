package mxstar.ast;

import mxstar.utility.Location;
import mxstar.type.Type;

public class TypeNode extends Node {
	private Type type;

	public TypeNode(Type type, Location location) {
		this.type = type;
		this.location = location;
	}

	public void setType(Type type) { this.type = type; }

	public Type getType() { return type; }

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
