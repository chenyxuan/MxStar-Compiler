package mxstar.frontend;

import mxstar.ast.*;
import mxstar.utility.UnESC;

import java.io.PrintStream;

public class ASTPrinter implements ASTVisitor {
	private StringBuilder indentStr = new StringBuilder();
	private PrintStream out;

	public ASTPrinter(PrintStream out) {
		this.out = out;
	}

	private void println(String str) {
		out.println(getIndentStr() + str);
	}
	private void printf(String str, Object... args) {
		out.printf(getIndentStr() + str, args);
	}


	private void indent() {
		indentStr.append('\t');
	}
	private void unindent() {
		indentStr.deleteCharAt(indentStr.lastIndexOf("\t"));
	}
	private String getIndentStr() {
		return indentStr.toString();
	}

	@Override
	public void visit(ASTRootNode node) {
		if (node.location() == null) System.err.println("???");
		printf("@ ASTRootNode %s:\n", node.location().toString());
		if (!(node.getDecls().isEmpty())) {
			println(">>> decls:");
			for (Node decl : node.getDecls()) {
				decl.accept(this);
			}
			println(">>> end of decls");
		}
		else {
			println(">>> decls: null");
		}
	}

	@Override
	public void visit(VarDeclListNode node) {
		indent();
		printf("@ VarDeclListNode %s:\n", node.location().toString());
		println(">>> decls");
		for (Node decl : node.getDecls())
			decl.accept(this);
		println(">>> end of decls");
		unindent();
	}

	@Override
	public void visit(FuncDefNode node) {
		indent();
		printf("@ FuncDeclNode %s:\n", node.location().toString());
		if (node.getReturnType() != null) {
			println(">>> returnType:");
			node.getReturnType().accept(this);
		}
		else {
			println(">>> returnType: null");
		}
		printf(">>> name: %s\n", node.getName());
		if (!(node.getParameterList().isEmpty())) {
			println(">>> parameterList:");
			for (VarDeclNode parameter : node.getParameterList()) {
				parameter.accept(this);
			}
			println(">>> end of parameterList");
		}
		else {
			println(">>> parameterList: null");
		}
		println(">>> body:");
		node.getBody().accept(this);
		unindent();
	}

	@Override
	public void visit(ClassDeclNode node) {
		indent();
		printf("@ ClassDeclNode %s:\n", node.location().toString());
		printf(">>> name: %s\n", node.getName());
		if (!(node.getVarMember().isEmpty())) {
			println(">>> varMember:");
			for (VarDeclListNode varMem : node.getVarMember()) {
				varMem.accept(this);
			}
			println(">>> end of varMem");
		}
		else {
			println(">>> varMember: null");
		}
		if (!(node.getFuncMember().isEmpty())) {
			println(">>> funcMember:");
			for (FuncDefNode funcMem : node.getFuncMember()) {
				funcMem.accept(this);
			}
			println(">>> end of funcMember");
		}
		else {
			println(">>> funcMember: null");
		}
		unindent();
	}

	@Override
	public void visit(VarDeclNode node) {
		indent();
		printf("@ VaeDeclNode %s:\n", node.location().toString());
		println(">>> type:");
		node.getType().accept(this);
		printf(">>> name: %s\n", node.getName());
		if (node.getInitVal() != null) {
			println(">>> init:");
			node.getInitVal().accept(this);
		}
		else {
			println(">>> init: null");
		}
		unindent();
	}

	@Override
	public void visit(BlockStmtNode node) {
		indent();
		printf("@ BlockStmtNode %s:\n", node.location().toString());
		if (!(node.getCompound().isEmpty())) {
			println(">>> compound:");
			for (Node item : node.getCompound()) {
				item.accept(this);
			}
			println(">>> end of compound");
		}
		else {
			println(">>> compound: null");
		}
		unindent();
	}

	@Override
	public void visit(CondStmtNode node) {
		indent();
		printf("@ CondStmtNode %s:\n", node.location().toString());
		println(">>> cond:");
		node.getCond().accept(this);
		if (node.getThenStmt() != null) {
			println(">>> thenStmt:");
			node.getThenStmt().accept(this);
		}
		else {
			println(">>> thenStmt: null");
		}
		if (node.getElseStmt() != null) {
			println(">>> elseStmt:");
			node.getElseStmt().accept(this);
		}
		else {
			println(">>> elseStmt: null");
		}
		unindent();
	}

	@Override
	public void visit(WhileStmtNode node) {
		indent();
		printf("@ WhileStmtNode %s:\n", node.location().toString());
		println(">>> cond:");
		node.getCond().accept(this);
		if (node.getStmt() != null) {
			println(">>> stmt:");
			node.getStmt().accept(this);
		}
		else {
			println(">>> stmt: null");
		}
		unindent();
	}

	@Override
	public void visit(ForStmtNode node) {
		indent();
		printf("@ ForStmtNode %s:\n", node.location().toString());
		if (node.getInit() != null) {
			println(">>> init:");
			node.getInit().accept(this);
		}
		else {
			println(">>> init: null");
		}
		if (node.getCond() != null) {
			println(">>> cond:");
			node.getCond().accept(this);
		}
		else {
			println(">>> cond: null");
		}
		if (node.getStep() != null) {
			println(">>> step:");
			node.getStep().accept(this);
		}
		else {
			println(">>> step: null");
		}
		if (node.getStmt() != null) {
			println(">>> stmt: ");
			node.getStmt().accept(this);
		}
		else {
			println(">>> stmt: null");
		}
		unindent();
	}

	@Override
	public void visit(ContinueStmtNode node) {
		indent();
		printf("@ ContinueStmtNode %s:\n", node.location().toString());
		unindent();
	}

	@Override
	public void visit(BreakStmtNode node) {
		indent();
		printf("@ BreakStmtNode %s:\n", node.location().toString());
		unindent();
	}

	@Override
	public void visit(ReturnStmtNode node) {
		indent();
		printf("@ ReturnStmtNode %s:\n", node.location().toString());
		if (node.getExpr() != null) {
			println(">>> expr:");
			node.getExpr().accept(this);
		}
		else {
			println(">>> expr: null");
		}
		unindent();
	}

	@Override
	public void visit(SuffixExprNode node) {
		indent();
		printf("@ SuffixExprNode %s:\n", node.location().toString());
		printf(">>> op: %s\n", node.getOp().toString());
		println(">>> expr:");
		node.getExpr().accept(this);
		unindent();
	}

	@Override
	public void visit(FuncCallExprNode node) {
		indent();
		printf("@ FuncCallExprNode %s:\n", node.location().toString());
		println(">>> func:");
		node.getFunc().accept(this);
		if (!(node.getArgs().isEmpty())) {
			println(">>> args:");
			for (ExprNode arg : node.getArgs()) {
				arg.accept(this);
			}
			println(">>> end of args");
		}
		else {
			println(">>> args: null");
		}
		unindent();
	}

	@Override
	public void visit(SubscriptExprNode node) {
		indent();
		printf("@ SubscriptExprNode %s:\n", node.location().toString());
		println(">>> arr:");
		node.getArray().accept(this);
		println(">>> sub:");
		node.getPostfix().accept(this);
		unindent();
	}

	@Override
	public void visit(MemberAccessExprNode node) {
		indent();
		printf("@ MemberAccessExprNode %s:\n", node.location().toString());
		println(">>> expr:");
		node.getExpr().accept(this);
		printf(">>> member: %s\n", node.getMember());
		unindent();
	}

	@Override
	public void visit(PrefixExprNode node) {
		indent();
		printf("@ PrefixExprNode %s:\n", node.location().toString());
		printf(">>> op: %s\n", node.getOp().toString());
		println(">>> expr:");
		node.getExpr().accept(this);
		unindent();
	}

	@Override
	public void visit(NewExprNode node) {
		indent();
		printf("@ NewExprNode %s:\n", node.location().toString());
		println(">>> newType:");
		node.getNewType().accept(this);
		if (node.getDims() != null) {
			println(">>> dims:");
			for (ExprNode dim : node.getDims()) {
				dim.accept(this);
			}
			println(">>> end of dims");
		}
		unindent();
	}

	@Override
	public void visit(BinaryExprNode node) {
		indent();
		printf("@ BinaryExprNode %s:\n", node.location().toString());
		printf(">>> op: %s\n", node.getOp().toString());
		println(">>> lhs:");
		node.getLhs().accept(this);
		println(">>> rhs:");
		node.getRhs().accept(this);
		unindent();
	}

	@Override
	public void visit(AssignExprNode node) {
		indent();
		printf("@ AssignExprNode %s:\n", node.location().toString());
		println(">>> lhs:");
		node.getLhs().accept(this);
		println(">>> rhs:");
		node.getRhs().accept(this);
		unindent();
	}

	@Override
	public void visit(IdentifierExprNode node) {
		indent();
		printf("@ IdentifierExprNode %s:\n", node.location().toString());
		printf(">>> identifier: %s\n", node.getID());
		unindent();
	}

	@Override
	public void visit(ThisExprNode node) {
		indent();
		printf("@ ThisExprNode %s:\n", node.location().toString());
		unindent();
	}

	@Override
	public void visit(IntConstNode node) {
		indent();
		printf("@ IntConstExprNode %s:\n", node.location().toString());
		printf(">>> value: %d\n", node.getValue());
		unindent();
	}

	@Override
	public void visit(StringConstNode node) {
		indent();
		printf("@ StringConstExprNode %s:\n", node.location().toString());
		printf(">>> value: %s\n", UnESC.enEsc(node.getValue()));
		unindent();
	}

	@Override
	public void visit(BoolConstNode node) {
		indent();
		printf("@ BoolConstExprNode %s:\n", node.location().toString());
		printf(">>> value: %b\n", node.getValue());
		unindent();
	}

	@Override
	public void visit(NullLiteralNode node) {
		indent();
		printf("@ NullExprNode %s:\n", node.location().toString());
		unindent();
	}

	@Override
	public void visit(TypeNode node) {
		indent();
		printf("@ TypeNode %s:\n", node.location().toString());
		printf(">>> type: %s\n", node.getType().toString());
		unindent();
	}
}
