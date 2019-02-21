package mxstar.type;

public class VoidType extends PrimitiveType {
	private static VoidType instance = new VoidType();

	public VoidType() {
		hyperType = HyperTypes.VOID;
		varSize = 0;
	}

	public static VoidType getInstance() { return instance; }
}
