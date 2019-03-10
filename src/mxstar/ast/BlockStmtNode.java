package mxstar.ast;

import mxstar.symbol.scope.Scope;
import mxstar.utility.Location;

import java.util.List;

public class BlockStmtNode extends StmtNode {
	private List<Node> compound;
	private Scope scope;

	public BlockStmtNode(List<Node> compound, Location location) {
		this.compound = compound;
		this.location = location;
		this.scope = null;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public Scope getScope() {
		return scope;
	}

	public List<Node> getCompound() { return compound; }

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
