package mxstar.ast;

import mxstar.utility.Location;

import java.util.List;

public class VarDeclListNode extends Node {
	private List<VarDeclNode> decls;

	public VarDeclListNode(List<VarDeclNode> decls, Location location) {
		this.decls = decls;
		this.location = location;
	}

	public List<VarDeclNode> getDecls() { return decls; }

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
