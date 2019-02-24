package mxstar.type;

import static mxstar.utility.GlobalSymbols.*;

public class IntType extends PrimitiveType {
	private static IntType instance = new IntType();

	public IntType() {
		hyperType = HyperTypes.INT;
		varSize = REG_SIZE;
	}

	public static IntType getInstance() { return instance; }
}
