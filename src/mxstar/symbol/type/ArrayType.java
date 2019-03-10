package mxstar.symbol.type;

import static mxstar.utility.GlobalSymbols.*;

public class ArrayType extends Type {
	private Type baseType;

	public ArrayType(Type baseType) {
		hyperType = HyperTypes.ARRAY;
		this.baseType = baseType;
		varSize = REG_SIZE;
	}

	public static ArrayType gen(Type pileType, int numDim) {
		if(numDim <= 0) throw new Error("Can't generate ArrayType with numDim <= 0");

		for(int i = 0; i < numDim; i++) {
			pileType = new ArrayType(pileType);
		}
		return (ArrayType) pileType;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ArrayType)) return false;
		return baseType.equals(((ArrayType) obj).baseType);
	}

	@Override
	public String toString() {
		return String.format("ArrayType(%s)", baseType.toString());
	}

	public Type getBaseType() { return baseType; }
}
