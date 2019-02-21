package mxstar.type;

import static mxstar.utility.GlobalSymbols.*;

public class ClassType extends Type {
	private String name;

	public ClassType(String name) {
		hyperType = HyperTypes.CLASS;
		varSize = REG_SIZE;
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ClassType)) return false;
		return name.equals(((ClassType) obj).name);
	}

	@Override
	public String toString() {
		return String.format("ClassType(%s)", name);
	}

	public String getName() { return name; }
}
