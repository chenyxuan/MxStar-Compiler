package mxstar.scope;

import mxstar.type.Type;

abstract public class Entity {
	private Type type;
	private String name;

	public Entity(Type type, String name) {
		this.type = type;
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}
}
