package mxstar.symbol.type;
import static mxstar.utility.GlobalSymbols.*;

public class BoolType extends PrimitiveType {
	private static BoolType instance = new BoolType();

	public BoolType() {
		hyperType = HyperTypes.BOOL;
		varSize = REG_SIZE;
	}

	public static BoolType getInstance() { return instance; }
}
