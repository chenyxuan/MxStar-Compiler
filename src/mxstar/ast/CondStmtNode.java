package mxstar.ast;

import mxstar.utility.Location;

public class CondStmtNode extends JumpStmtNode {
	private ExprNode cond;
	private StmtNode thenStmt, elseStmt;

	public CondStmtNode(ExprNode cond, StmtNode thenStmt, StmtNode elseStmt, Location location) {
		this.cond = cond;
		this.thenStmt = thenStmt;
		this.elseStmt = elseStmt;
		this.location = location;
	}

	public ExprNode getCond() { return cond; }

	public StmtNode getThenStmt() { return thenStmt; }

	public StmtNode getElseStmt() { return elseStmt; }

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
