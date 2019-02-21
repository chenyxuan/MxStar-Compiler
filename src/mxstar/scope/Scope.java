package mxstar.scope;

import mxstar.utility.Location;
import mxstar.utility.error.SemanticError;

import java.util.HashMap;
import java.util.Map;

public class Scope {
	private Map<String, Entity> entityMap = new HashMap<>();
	private Scope parent;

	public Scope(Scope parent) {
		this.parent = parent;
	}

	public Scope() {
		this(null);
	}

	public Map<String, Entity> getEntityMap() {
		return entityMap;
	}

	public Scope getParent() {
		return parent;
	}

	public boolean contains(String key) {
		if(entityMap.containsKey(key)) return true;
		if(parent != null) return parent.contains(key);
		return false;
	}

	public boolean insert(String key, Entity entity) {
		if(entityMap.containsKey(key)) return false;
		entityMap.put(key, entity);
		return true;
	}

	public Entity scratch(String key) {
		if(entityMap.containsKey(key)) return entityMap.get(key);
		return null;
	}

	public Entity find(String key) {
		if(entityMap.containsKey(key)) return entityMap.get(key);
		if(parent != null) return parent.find(key);
		return null;
	}

	public void assertContains(String key, Location location) {
		if(!contains(key)) throw new SemanticError(String.format("Entity %s not found", key), location);
	}

	public void assertInsert(String key, Entity entity, Location location) {
		if(!insert(key, entity)) throw new SemanticError(String.format("Entity %s is already defined", key), location);
	}
}
