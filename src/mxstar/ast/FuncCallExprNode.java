package mxstar.ast;

import mxstar.symbol.scope.FuncEntity;
import mxstar.utility.Location;

import java.util.List;

public class FuncCallExprNode extends ExprNode {
	private ExprNode func;
	private List<ExprNode> args;
	private FuncEntity funcEntity;

	public FuncCallExprNode(ExprNode func, List<ExprNode> args, Location location) {
		this.func = func;
		this.args = args;
		this.location = location;
	}

	public ExprNode getFunc() {
		return func;
	}

	public List<ExprNode> getArgs() {
		return args;
	}

	public void setFuncEntity(FuncEntity funcEntity) {
		this.funcEntity = funcEntity;
	}

	public FuncEntity getFuncEntity() {
		return funcEntity;
	}

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
