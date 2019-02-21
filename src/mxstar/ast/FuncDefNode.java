package mxstar.ast;


import mxstar.scope.FuncEntity;
import mxstar.utility.Location;

import java.util.List;

public class FuncDefNode extends DeclNode {
	private TypeNode returnType;
	private List<VarDeclNode> parameterList;
	private BlockStmtNode body;

	private FuncEntity funcEntity = null;

	public FuncDefNode(TypeNode returnType,
	                   String name,
	                   List<VarDeclNode> parameterList,
	                   BlockStmtNode body,
	                   Location location) {
		this.returnType = returnType;
		this.name = name;
		this.parameterList = parameterList;
		this.body = body;
		this.location = location;
	}

	public void setFuncEntity(FuncEntity funcEntity) {
		this.funcEntity = funcEntity;
	}

	public FuncEntity getFuncEntity() {
		return funcEntity;
	}

	public TypeNode getReturnType() { return returnType; }

	public List<VarDeclNode> getParameterList() { return parameterList; }

	public BlockStmtNode getBody() { return body; }

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
