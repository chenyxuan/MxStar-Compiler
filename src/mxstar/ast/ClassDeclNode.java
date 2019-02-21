package mxstar.ast;

import mxstar.scope.ClassEntity;
import mxstar.scope.Scope;
import mxstar.utility.Location;

import java.util.List;

public class ClassDeclNode extends DeclNode {
	private List<VarDeclListNode> varMember;
	private List<FuncDefNode> funcMember;

	private Scope scope = null;
	private ClassEntity classEntity = null;

	public ClassDeclNode(String name,
	                     List<VarDeclListNode> varMember,
	                     List<FuncDefNode> funcMember,
	                     Location location) {
		this.name = name;
		this.varMember = varMember;
		this.funcMember = funcMember;
		this.location = location;
	}

	public void setClassEntity(ClassEntity classEntity) {
		this.classEntity = classEntity;
	}

	public ClassEntity getClassEntity() {
		return classEntity;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public Scope getScope() {
		return scope;
	}

	public List<VarDeclListNode> getVarMember() { return varMember; }

	public List<FuncDefNode> getFuncMember() { return funcMember; }

	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
}
