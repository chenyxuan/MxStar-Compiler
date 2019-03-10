package mxstar.symbol.type;

public class NullLiteral extends Type {
	static private NullLiteral instance = new NullLiteral();

	private NullLiteral() {
		hyperType = HyperTypes.NULL;
		varSize = 0;
	}

	public static NullLiteral getInstance() {
		return instance;
	}

	@Override
	public String toString() {
		return "NullLiteral";
	}
}
