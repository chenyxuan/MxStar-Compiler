package mxstar.scope;

import mxstar.ast.ClassDeclNode;
import mxstar.type.ClassType;
import mxstar.type.Type;

public class ClassEntity extends Entity {
	private Scope scope = null;
	private int width = 0;

	public ClassEntity(Type type, String name) {
		super(type, name);
	}

	public ClassEntity(ClassDeclNode node) {
		this(new ClassType(node.getName()), node.getName());
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public Scope getScope() {
		return scope;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}
}
