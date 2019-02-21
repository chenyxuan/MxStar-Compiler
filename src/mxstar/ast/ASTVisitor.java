package mxstar.ast;


public interface ASTVisitor {
	void visit(AssignExprNode node);
	void visit(BinaryExprNode node);
	void visit(BlockStmtNode node);
	void visit(BoolConstNode node);
	void visit(BreakStmtNode node);
	void visit(ClassDeclNode node);
	void visit(CondStmtNode node);
	void visit(ContinueStmtNode node);
	void visit(ForStmtNode node);
	void visit(FuncCallExprNode node);
	void visit(FuncDefNode node);
	void visit(IdentifierExprNode node);
	void visit(IntConstNode node);
	void visit(MemberAccessExprNode node);
	void visit(NewExprNode node);
	void visit(NullLiteralNode node);
	void visit(PrefixExprNode node);
	void visit(ReturnStmtNode node);
	void visit(StringConstNode node);
	void visit(SubscriptExprNode node);
	void visit(SuffixExprNode node);
	void visit(ThisExprNode node);
	void visit(ASTRootNode node);
	void visit(TypeNode node);
	void visit(VarDeclNode node);
	void visit(VarDeclListNode node);
	void visit(WhileStmtNode node);
}
