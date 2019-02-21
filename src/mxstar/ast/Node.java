package mxstar.ast;

import mxstar.utility.Location;

abstract public class Node {
	protected Location location;

	public Location location() { return location; }

	abstract public void accept(ASTVisitor visitor);
}
