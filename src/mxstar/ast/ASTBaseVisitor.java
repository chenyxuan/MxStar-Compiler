package mxstar.ast;

public class ASTBaseVisitor implements ASTVisitor {

	protected void safeAccept(Node node) {
		if(node != null) node.accept(this);
	}

	@Override
	public void visit(ASTRootNode node) {
		for (Node e : node.getDecls()) {
			safeAccept(e);
		}
	}

	@Override
	public void visit(VarDeclListNode node) {
		for(Node e : node.getDecls()) {
			safeAccept(e);
		}
	}

	@Override
	public void visit(FuncDefNode node) {
		safeAccept(node.getReturnType());

		for(Node e : node.getParameterList()) {
			safeAccept(e);
		}

		safeAccept(node.getBody());
	}

	@Override
	public void visit(ClassDeclNode node) {
		for(Node e : node.getVarMember()) {
			safeAccept(e);
		}

		for(Node e : node.getFuncMember()) {
			safeAccept(e);
		}
	}

	@Override
	public void visit(BlockStmtNode node) {
		for(Node e : node.getCompound()) {
			safeAccept(e);
		}
	}

	@Override
	public void visit(ForStmtNode node) {
		safeAccept(node.getInit());
		safeAccept(node.getCond());
		safeAccept(node.getStep());
		safeAccept(node.getStmt());
	}

	@Override
	public void visit(CondStmtNode node) {
		safeAccept(node.getCond());
		safeAccept(node.getThenStmt());
		safeAccept(node.getElseStmt());
	}

	@Override
	public void visit(NewExprNode node) {
		safeAccept(node.getNewType());
		for(Node e : node.getDims()) {
			safeAccept(e);
		}
	}

	@Override
	public void visit(VarDeclNode node) {
		safeAccept(node.getType());
		safeAccept(node.getInitVal());
	}

	@Override
	public void visit(MemberAccessExprNode node) {
		safeAccept(node.getExpr());
	}

	@Override
	public void visit(WhileStmtNode node) {
		safeAccept(node.getCond());
		safeAccept(node.getStmt());
	}

	@Override
	public void visit(AssignExprNode node) {
		safeAccept(node.getLhs());
		safeAccept(node.getRhs());
	}

	@Override
	public void visit(BinaryExprNode node) {
		safeAccept(node.getLhs());
		safeAccept(node.getRhs());
	}

	@Override
	public void visit(PrefixExprNode node) {
		safeAccept(node.getExpr());
	}

	@Override
	public void visit(ReturnStmtNode node) {
		safeAccept(node.getExpr());
	}

	@Override
	public void visit(SuffixExprNode node) {
		safeAccept(node.getExpr());
	}


	@Override
	public void visit(FuncCallExprNode node) {
		safeAccept(node.getFunc());
		for(Node e : node.getArgs()) {
			safeAccept(e);
		}
	}

	@Override
	public void visit(SubscriptExprNode node) {
		safeAccept(node.getArray());
		safeAccept(node.getPostfix());
	}

	@Override
	public void visit(TypeNode node) {
	}

	@Override
	public void visit(IdentifierExprNode node) {
	}

	@Override
	public void visit(ThisExprNode node) {
	}

	@Override
	public void visit(BoolConstNode node) {
	}

	@Override
	public void visit(BreakStmtNode node) {
	}

	@Override
	public void visit(NullLiteralNode node) {
	}

	@Override
	public void visit(StringConstNode node) {
	}

	@Override
	public void visit(ContinueStmtNode node) {
	}

	@Override
	public void visit(IntConstNode node) {
	}

}
