package mxstar.frontend;

import mxstar.ast.*;
import mxstar.parser.MxStarBaseVisitor;
import mxstar.parser.MxStarParser;
import mxstar.utility.*;
import mxstar.utility.error.CompilationError;
import mxstar.symbol.type.*;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

public class ASTBuilder extends MxStarBaseVisitor<Node> {

	public Node visitTranslationUnit(MxStarParser.TranslationUnitContext ctx) {
		List<Node> decls = new ArrayList<>();

		if (ctx.externalDeclaration() != null) {
			for (ParserRuleContext e : ctx.externalDeclaration()) {
				decls.add(visit(e));
			}
		}

		return new ASTRootNode(decls, Location.fromCtx(ctx));
	}

	public Node visitFunctionDefinition(MxStarParser.FunctionDefinitionContext ctx) {
		TypeNode returnType = null;
		String name = ctx.ID().getText();
		List<VarDeclNode> parameterList = new ArrayList<>();

		if (ctx.typeSpecifier() != null) returnType = (TypeNode) visit(ctx.typeSpecifier());

		if (ctx.parameterDeclarationList() != null) {
			for (ParserRuleContext e : ctx.parameterDeclarationList().parameterDeclaration()) {
				parameterList.add((VarDeclNode) visit(e));
			}
		}
		BlockStmtNode body = (BlockStmtNode) visit(ctx.blockStatement());

		return new FuncDefNode(returnType, name, parameterList, body, Location.fromCtx(ctx));
	}

	public Node visitParameterDeclaration(MxStarParser.ParameterDeclarationContext ctx) {
		TypeNode type = (TypeNode) visit(ctx.typeSpecifier());
		String name = ctx.ID().getText();

		return new VarDeclNode(type, name, null, Location.fromCtx(ctx));
	}

	public Node visitVariableDeclaration(MxStarParser.VariableDeclarationContext ctx) {
		TypeNode type = (TypeNode) visit(ctx.typeSpecifier());
		List<VarDeclNode> decls = new ArrayList<>();

		if (ctx.variableDeclarationList() != null) {
			for (MxStarParser.VariableDeclaratorContext e : ctx.variableDeclarationList().variableDeclarator()) {
				String name = e.ID().getText();
				ExprNode initVal = null;

				if (e.expression() != null) initVal = (ExprNode) visit(e.expression());
				decls.add(new VarDeclNode(type, name, initVal, Location.fromCtx(e)));
			}
		}

		return new VarDeclListNode(decls, Location.fromCtx(ctx));
	}

	public Node visitClassDeclaration(MxStarParser.ClassDeclarationContext ctx) {
		String name = ctx.ID().getText();
		List<VarDeclListNode> varDeclLists = new ArrayList<>();
		List<FuncDefNode> funcDefs = new ArrayList<>();

		if (ctx.variableDeclaration() != null) {
			for (MxStarParser.VariableDeclarationContext e : ctx.variableDeclaration()) {
				varDeclLists.add((VarDeclListNode) visit(e));
			}
		}

		if (ctx.functionDefinition() != null) {
			for (MxStarParser.FunctionDefinitionContext e : ctx.functionDefinition()) {
				funcDefs.add((FuncDefNode) visit(e));
			}
		}

		return new ClassDeclNode(name, varDeclLists, funcDefs, Location.fromCtx(ctx));
	}

	public Node visitTypeSpecifier(MxStarParser.TypeSpecifierContext ctx) {
		if (ctx.nonVoidType() != null) return visit(ctx.nonVoidType());
		return new TypeNode(VoidType.getInstance(), Location.fromCtx(ctx));
	}

	public Node visitNonArrayType(MxStarParser.NonArrayTypeContext ctx) {
		return new TypeNode(TypeOfCtx.typeOfCtx(ctx.ID(), ctx.INT(), ctx.BOOL(), ctx.STRING()), Location.fromCtx(ctx));
	}

	public Node visitArrayType(MxStarParser.ArrayTypeContext ctx) {
		return new TypeNode(ArrayType.gen(((TypeNode) visit(ctx.nonArrayType())).getType(),
				(ctx.getChildCount() - 1) / 2), Location.fromCtx(ctx));
	}

	public Node visitBlockStatement(MxStarParser.BlockStatementContext ctx) {
		List<Node> compound = new ArrayList<>();

		if (ctx.blockCompound() != null) {
			for (ParserRuleContext e : ctx.blockCompound()) {
				Node child = visit(e);
				if (child != null) compound.add(child);
			}
		}

		return new BlockStmtNode(compound, Location.fromCtx(ctx));
	}

	public Node visitConditionStatement(MxStarParser.ConditionStatementContext ctx) {
		ExprNode cond = (ExprNode) visit(ctx.expression());
		StmtNode thenStmt = (StmtNode) visit(ctx.thenStmt);
		StmtNode elseStmt = null;
		if (ctx.elseStmt != null) elseStmt = (StmtNode) visit(ctx.elseStmt);
		return new CondStmtNode(cond, thenStmt, elseStmt, Location.fromCtx(ctx));
	}

	public Node visitForStmt(MxStarParser.ForStmtContext ctx) {
		ExprNode init = null, cond = null, step = null;
		StmtNode stmt = (StmtNode) visit(ctx.statement());

		if (ctx.init != null) init = (ExprNode) visit(ctx.init);
		if (ctx.cond != null) cond = (ExprNode) visit(ctx.cond);
		if (ctx.step != null) step = (ExprNode) visit(ctx.step);

		return new ForStmtNode(init, cond, step, stmt, Location.fromCtx(ctx));
	}

	public Node visitWhileStmt(MxStarParser.WhileStmtContext ctx) {
		ExprNode cond = (ExprNode) visit(ctx.expression());
		StmtNode stmt = (StmtNode) visit(ctx.statement());
		return new WhileStmtNode(cond, stmt, Location.fromCtx(ctx));
	}

	public Node visitContinueStmt(MxStarParser.ContinueStmtContext ctx) {
		return new ContinueStmtNode(Location.fromCtx(ctx));
	}

	public Node visitBreakStmt(MxStarParser.BreakStmtContext ctx) {
		return new BreakStmtNode(Location.fromCtx(ctx));
	}

	public Node visitReturnStmt(MxStarParser.ReturnStmtContext ctx) {
		ExprNode expr = null;
		if (ctx.expression() != null) expr = (ExprNode) visit(ctx.expression());
		return new ReturnStmtNode(expr, Location.fromCtx(ctx));
	}

	public Node visitPrefixExpr(MxStarParser.PrefixExprContext ctx) {
		PrefixExprNode.Ops op = null;

		switch (ctx.op.getText()) {
			case "++":
				op = PrefixExprNode.Ops.INC;
				break;
			case "--":
				op = PrefixExprNode.Ops.DEC;
				break;
			case "+":
				op = PrefixExprNode.Ops.POS;
				break;
			case "-":
				op = PrefixExprNode.Ops.NEG;
				break;
			case "!":
				op = PrefixExprNode.Ops.LOG_NOT;
				break;
			case "~":
				op = PrefixExprNode.Ops.BIT_NOT;
				break;
			default:
				assert false;
		}
		ExprNode expr = (ExprNode) visit(ctx.expression());
		return new PrefixExprNode(op, expr, Location.fromCtx(ctx));
	}

	public Node visitSubscriptExpr(MxStarParser.SubscriptExprContext ctx) {
		ExprNode array = (ExprNode) visit(ctx.array);
		ExprNode postfix = (ExprNode) visit(ctx.postfix);
		return new SubscriptExprNode(array, postfix, Location.fromCtx(ctx));
	}

	public Node visitSuffixExpr(MxStarParser.SuffixExprContext ctx) {
		SuffixExprNode.Ops op = null;
		switch (ctx.op.getText()) {
			case "++":
				op = SuffixExprNode.Ops.INC;
				break;
			case "--":
				op = SuffixExprNode.Ops.DEC;
				break;
			default:
				assert false;
		}
		ExprNode expr = (ExprNode) visit(ctx.expression());
		return new SuffixExprNode(op, expr, Location.fromCtx(ctx));
	}

	public Node visitBinaryExpr(MxStarParser.BinaryExprContext ctx) {
		BinaryExprNode.Ops op = null;
		switch (ctx.op.getText()) {
			case "*":
				op = BinaryExprNode.Ops.MUL;
				break;
			case "/":
				op = BinaryExprNode.Ops.DIV;
				break;
			case "%":
				op = BinaryExprNode.Ops.MOD;
				break;
			case "+":
				op = BinaryExprNode.Ops.ADD;
				break;
			case "-":
				op = BinaryExprNode.Ops.SUB;
				break;
			case "<<":
				op = BinaryExprNode.Ops.SHL;
				break;
			case ">>":
				op = BinaryExprNode.Ops.SHR;
				break;
			case "<":
				op = BinaryExprNode.Ops.LT;
				break;
			case ">":
				op = BinaryExprNode.Ops.GT;
				break;
			case "<=":
				op = BinaryExprNode.Ops.LT;
				break;
			case ">=":
				op = BinaryExprNode.Ops.GT;
				break;
			case "==":
				op = BinaryExprNode.Ops.EQ;
				break;
			case "!=":
				op = BinaryExprNode.Ops.NEQ;
				break;
			case "&":
				op = BinaryExprNode.Ops.BIT_AND;
				break;
			case "^":
				op = BinaryExprNode.Ops.BIT_XOR;
				break;
			case "|":
				op = BinaryExprNode.Ops.BIT_OR;
				break;
			case "&&":
				op = BinaryExprNode.Ops.LOG_AND;
				break;
			case "||":
				op = BinaryExprNode.Ops.LOG_OR;
				break;
			default:
				assert false;
		}
		ExprNode lhs = (ExprNode) visit(ctx.lhs);
		ExprNode rhs = (ExprNode) visit(ctx.rhs);
		return new BinaryExprNode(op, lhs, rhs, Location.fromCtx(ctx));
	}

	public Node visitMemberAccessExpr(MxStarParser.MemberAccessExprContext ctx) {
		ExprNode expr = (ExprNode) visit(ctx.expression());
		String member = ctx.ID().getText();

		return new MemberAccessExprNode(expr, member, Location.fromCtx(ctx));
	}

	public Node visitFuncCallExpr(MxStarParser.FuncCallExprContext ctx) {
		ExprNode func = (ExprNode) visit(ctx.expression());
		List<ExprNode> args = new ArrayList<>();

		if (ctx.parameterList() != null) {
			for (ParserRuleContext e : ctx.parameterList().expression()) {
				args.add((ExprNode) visit(e));
			}
		}

		return new FuncCallExprNode(func, args, Location.fromCtx(ctx));
	}

	public Node visitAssignExpr(MxStarParser.AssignExprContext ctx) {
		ExprNode lhs = (ExprNode) visit(ctx.lhs);
		ExprNode rhs = (ExprNode) visit(ctx.rhs);
		return new AssignExprNode(lhs, rhs, Location.fromCtx(ctx));
	}

	public Node visitIdentifierExpr(MxStarParser.IdentifierExprContext ctx) {
		return new IdentifierExprNode(ctx.ID().getText(), Location.fromCtx(ctx));
	}

	public Node visitThisExpr(MxStarParser.ThisExprContext ctx) {
		return new ThisExprNode(Location.fromCtx(ctx));
	}

	public Node visitNewExpr(MxStarParser.NewExprContext ctx) {
		assert ctx.creator() != null;
		return visit(ctx.creator());
	}

	public Node visitIntConst(MxStarParser.IntConstContext ctx) {
		int value = Integer.parseInt(ctx.INTEGER_CONST().getText());
		return new IntConstNode(value, Location.fromCtx(ctx));
	}

	public Node visitStringConst(MxStarParser.StringConstContext ctx) {
		String str = ctx.STRING_CONST().getText();

		try {
			str = UnESC.unEsc(str);
		} catch (Error error) {
			throw new CompilationError(error.getMessage(), Location.fromCtx(ctx));
		}

		return new StringConstNode(str, Location.fromCtx(ctx));
	}

	public Node visitNullLiteral(MxStarParser.NullLiteralContext ctx) {
		return new NullLiteralNode(Location.fromCtx(ctx));
	}

	public Node visitBoolConst(MxStarParser.BoolConstContext ctx) {
		boolean value = ctx.BOOL_CONST().getText().equals("true");
		return new BoolConstNode(value, Location.fromCtx(ctx));
	}

	public Node visitNonArrayCreator(MxStarParser.NonArrayCreatorContext ctx) {
		TypeNode newType = new TypeNode(TypeOfCtx.typeOfCtx(ctx.ID(), ctx.INT(), ctx.BOOL(), ctx.STRING()),
				Location.fromCtx(ctx));
		return new NewExprNode(newType, null, Location.fromCtx(ctx));
	}

	public Node visitArrayCreator(MxStarParser.ArrayCreatorContext ctx) {
		TypeNode newType = ((NewExprNode) visit(ctx.nonArrayCreator())).getNewType();
		List<ExprNode> dims = new ArrayList<>();

		if (ctx.expression() != null) {
			for (ParserRuleContext dim : ctx.expression()) {
				dims.add((ExprNode) visit(dim));
			}
		}
		newType.setType(ArrayType.gen(newType.getType(),
				(ctx.getChildCount() - 1 - dims.size()) / 2));
		return new NewExprNode(newType, dims, Location.fromCtx(ctx));
	}

	public Node visitErrorCreator(MxStarParser.ErrorCreatorContext ctx) {
		throw new CompilationError("Error Creator format", Location.fromCtx(ctx));
	}

	public Node visitStatement(MxStarParser.StatementContext ctx) {
		if (ctx.expression() != null) return visit(ctx.expression());
		return super.visitStatement(ctx);
	}

	public Node visitSubExpr(MxStarParser.SubExprContext ctx) {
		return visit(ctx.expression());
	}

	@Override
	public Node visitVariableDeclarator(MxStarParser.VariableDeclaratorContext ctx) {
		return super.visitVariableDeclarator(ctx);
	}

	@Override
	public Node visitVariableDeclarationList(MxStarParser.VariableDeclarationListContext ctx) {
		return super.visitVariableDeclarationList(ctx);
	}

	@Override
	public Node visitParameterList(MxStarParser.ParameterListContext ctx) {
		return super.visitParameterList(ctx);
	}

	@Override
	public Node visitParameterDeclarationList(MxStarParser.ParameterDeclarationListContext ctx) {
		return super.visitParameterDeclarationList(ctx);
	}

	@Override
	public Node visitPrimaryExpr(MxStarParser.PrimaryExprContext ctx) {
		return super.visitPrimaryExpr(ctx);
	}

	@Override
	public Node visitBlockCompound(MxStarParser.BlockCompoundContext ctx) {
		return super.visitBlockCompound(ctx);
	}

	@Override
	public Node visitOtherCreator(MxStarParser.OtherCreatorContext ctx) {
		return super.visitOtherCreator(ctx);
	}

	@Override
	public Node visitOtherType(MxStarParser.OtherTypeContext ctx) {
		return super.visitOtherType(ctx);
	}

	@Override
	public Node visitConstExpr(MxStarParser.ConstExprContext ctx) {
		return super.visitConstExpr(ctx);
	}

	@Override
	public Node visitExternalDeclaration(MxStarParser.ExternalDeclarationContext ctx) {
		return super.visitExternalDeclaration(ctx);
	}
}