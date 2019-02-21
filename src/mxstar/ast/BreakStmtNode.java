package mxstar.ast;

import mxstar.utility.Location;

public class BreakStmtNode extends JumpStmtNode {
	public BreakStmtNode(Location location) {
		this.location = location;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
