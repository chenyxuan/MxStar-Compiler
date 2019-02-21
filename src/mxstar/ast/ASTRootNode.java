package mxstar.ast;

import mxstar.utility.Location;

import java.util.List;

public class ASTRootNode extends Node {
	private List<Node> decls;

	public ASTRootNode(List<Node> dels, Location location) {
		this.decls = dels;
		this.location = location;
	}

	public List<Node> getDecls() { return decls; }

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
