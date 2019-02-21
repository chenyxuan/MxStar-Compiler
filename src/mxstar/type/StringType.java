package mxstar.type;

import static mxstar.utility.GlobalSymbols.*;

public class StringType extends PrimitiveType {
	private static StringType instance = new StringType();

	public StringType() {
		hyperType = HyperTypes.STRING;
		varSize = REG_SIZE;
	}

	public static StringType getInstance() { return instance; }
}
