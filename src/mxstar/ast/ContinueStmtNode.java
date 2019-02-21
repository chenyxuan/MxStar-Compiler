package mxstar.ast;

import mxstar.utility.Location;

public class ContinueStmtNode extends JumpStmtNode {
	public ContinueStmtNode(Location location) {
		this.location = location;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
