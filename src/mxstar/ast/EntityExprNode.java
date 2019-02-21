package mxstar.ast;

import mxstar.scope.Entity;

abstract public class EntityExprNode extends ExprNode {
	private Entity entity = null;

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}
}
