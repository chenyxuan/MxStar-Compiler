// Generated from MxStar.g4 by ANTLR 4.7.1
package mxstar.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MxStarParser}.
 */
public interface MxStarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MxStarParser#translationUnit}.
	 * @param ctx the parse tree
	 */
	void enterTranslationUnit(MxStarParser.TranslationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#translationUnit}.
	 * @param ctx the parse tree
	 */
	void exitTranslationUnit(MxStarParser.TranslationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterExternalDeclaration(MxStarParser.ExternalDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitExternalDeclaration(MxStarParser.ExternalDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefinition(MxStarParser.FunctionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefinition(MxStarParser.FunctionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#parameterDeclarationList}.
	 * @param ctx the parse tree
	 */
	void enterParameterDeclarationList(MxStarParser.ParameterDeclarationListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#parameterDeclarationList}.
	 * @param ctx the parse tree
	 */
	void exitParameterDeclarationList(MxStarParser.ParameterDeclarationListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterParameterDeclaration(MxStarParser.ParameterDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitParameterDeclaration(MxStarParser.ParameterDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterClassDeclaration(MxStarParser.ClassDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#classDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitClassDeclaration(MxStarParser.ClassDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclaration(MxStarParser.VariableDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#variableDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclaration(MxStarParser.VariableDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#variableDeclarationList}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclarationList(MxStarParser.VariableDeclarationListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#variableDeclarationList}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclarationList(MxStarParser.VariableDeclarationListContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#variableDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterVariableDeclarator(MxStarParser.VariableDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#variableDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitVariableDeclarator(MxStarParser.VariableDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeSpecifier(MxStarParser.TypeSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeSpecifier(MxStarParser.TypeSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arrayType}
	 * labeled alternative in {@link MxStarParser#nonVoidType}.
	 * @param ctx the parse tree
	 */
	void enterArrayType(MxStarParser.ArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arrayType}
	 * labeled alternative in {@link MxStarParser#nonVoidType}.
	 * @param ctx the parse tree
	 */
	void exitArrayType(MxStarParser.ArrayTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code otherType}
	 * labeled alternative in {@link MxStarParser#nonVoidType}.
	 * @param ctx the parse tree
	 */
	void enterOtherType(MxStarParser.OtherTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code otherType}
	 * labeled alternative in {@link MxStarParser#nonVoidType}.
	 * @param ctx the parse tree
	 */
	void exitOtherType(MxStarParser.OtherTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#nonArrayType}.
	 * @param ctx the parse tree
	 */
	void enterNonArrayType(MxStarParser.NonArrayTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#nonArrayType}.
	 * @param ctx the parse tree
	 */
	void exitNonArrayType(MxStarParser.NonArrayTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(MxStarParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(MxStarParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatement(MxStarParser.BlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatement(MxStarParser.BlockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#blockCompound}.
	 * @param ctx the parse tree
	 */
	void enterBlockCompound(MxStarParser.BlockCompoundContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#blockCompound}.
	 * @param ctx the parse tree
	 */
	void exitBlockCompound(MxStarParser.BlockCompoundContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#conditionStatement}.
	 * @param ctx the parse tree
	 */
	void enterConditionStatement(MxStarParser.ConditionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#conditionStatement}.
	 * @param ctx the parse tree
	 */
	void exitConditionStatement(MxStarParser.ConditionStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code whileStmt}
	 * labeled alternative in {@link MxStarParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStmt(MxStarParser.WhileStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code whileStmt}
	 * labeled alternative in {@link MxStarParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStmt(MxStarParser.WhileStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code forStmt}
	 * labeled alternative in {@link MxStarParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void enterForStmt(MxStarParser.ForStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code forStmt}
	 * labeled alternative in {@link MxStarParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void exitForStmt(MxStarParser.ForStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code returnStmt}
	 * labeled alternative in {@link MxStarParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStmt(MxStarParser.ReturnStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code returnStmt}
	 * labeled alternative in {@link MxStarParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStmt(MxStarParser.ReturnStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code breakStmt}
	 * labeled alternative in {@link MxStarParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void enterBreakStmt(MxStarParser.BreakStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code breakStmt}
	 * labeled alternative in {@link MxStarParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void exitBreakStmt(MxStarParser.BreakStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code continueStmt}
	 * labeled alternative in {@link MxStarParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void enterContinueStmt(MxStarParser.ContinueStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code continueStmt}
	 * labeled alternative in {@link MxStarParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void exitContinueStmt(MxStarParser.ContinueStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code newExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNewExpr(MxStarParser.NewExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code newExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNewExpr(MxStarParser.NewExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code prefixExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPrefixExpr(MxStarParser.PrefixExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code prefixExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPrefixExpr(MxStarParser.PrefixExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpr(MxStarParser.PrimaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpr(MxStarParser.PrimaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subscriptExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSubscriptExpr(MxStarParser.SubscriptExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subscriptExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSubscriptExpr(MxStarParser.SubscriptExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code suffixExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSuffixExpr(MxStarParser.SuffixExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code suffixExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSuffixExpr(MxStarParser.SuffixExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryExpr(MxStarParser.BinaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryExpr(MxStarParser.BinaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code memberAccessExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMemberAccessExpr(MxStarParser.MemberAccessExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code memberAccessExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMemberAccessExpr(MxStarParser.MemberAccessExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code funcCallExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFuncCallExpr(MxStarParser.FuncCallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code funcCallExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFuncCallExpr(MxStarParser.FuncCallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAssignExpr(MxStarParser.AssignExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignExpr}
	 * labeled alternative in {@link MxStarParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAssignExpr(MxStarParser.AssignExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IdentifierExpr}
	 * labeled alternative in {@link MxStarParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierExpr(MxStarParser.IdentifierExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IdentifierExpr}
	 * labeled alternative in {@link MxStarParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierExpr(MxStarParser.IdentifierExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code thisExpr}
	 * labeled alternative in {@link MxStarParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterThisExpr(MxStarParser.ThisExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code thisExpr}
	 * labeled alternative in {@link MxStarParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitThisExpr(MxStarParser.ThisExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code constExpr}
	 * labeled alternative in {@link MxStarParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstExpr(MxStarParser.ConstExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constExpr}
	 * labeled alternative in {@link MxStarParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstExpr(MxStarParser.ConstExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subExpr}
	 * labeled alternative in {@link MxStarParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterSubExpr(MxStarParser.SubExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subExpr}
	 * labeled alternative in {@link MxStarParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitSubExpr(MxStarParser.SubExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(MxStarParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(MxStarParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code errorCreator}
	 * labeled alternative in {@link MxStarParser#creator}.
	 * @param ctx the parse tree
	 */
	void enterErrorCreator(MxStarParser.ErrorCreatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code errorCreator}
	 * labeled alternative in {@link MxStarParser#creator}.
	 * @param ctx the parse tree
	 */
	void exitErrorCreator(MxStarParser.ErrorCreatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arrayCreator}
	 * labeled alternative in {@link MxStarParser#creator}.
	 * @param ctx the parse tree
	 */
	void enterArrayCreator(MxStarParser.ArrayCreatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arrayCreator}
	 * labeled alternative in {@link MxStarParser#creator}.
	 * @param ctx the parse tree
	 */
	void exitArrayCreator(MxStarParser.ArrayCreatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code otherCreator}
	 * labeled alternative in {@link MxStarParser#creator}.
	 * @param ctx the parse tree
	 */
	void enterOtherCreator(MxStarParser.OtherCreatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code otherCreator}
	 * labeled alternative in {@link MxStarParser#creator}.
	 * @param ctx the parse tree
	 */
	void exitOtherCreator(MxStarParser.OtherCreatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link MxStarParser#nonArrayCreator}.
	 * @param ctx the parse tree
	 */
	void enterNonArrayCreator(MxStarParser.NonArrayCreatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link MxStarParser#nonArrayCreator}.
	 * @param ctx the parse tree
	 */
	void exitNonArrayCreator(MxStarParser.NonArrayCreatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code intConst}
	 * labeled alternative in {@link MxStarParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterIntConst(MxStarParser.IntConstContext ctx);
	/**
	 * Exit a parse tree produced by the {@code intConst}
	 * labeled alternative in {@link MxStarParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitIntConst(MxStarParser.IntConstContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringConst}
	 * labeled alternative in {@link MxStarParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterStringConst(MxStarParser.StringConstContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringConst}
	 * labeled alternative in {@link MxStarParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitStringConst(MxStarParser.StringConstContext ctx);
	/**
	 * Enter a parse tree produced by the {@code boolConst}
	 * labeled alternative in {@link MxStarParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterBoolConst(MxStarParser.BoolConstContext ctx);
	/**
	 * Exit a parse tree produced by the {@code boolConst}
	 * labeled alternative in {@link MxStarParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitBoolConst(MxStarParser.BoolConstContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link MxStarParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterNullLiteral(MxStarParser.NullLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link MxStarParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitNullLiteral(MxStarParser.NullLiteralContext ctx);
}