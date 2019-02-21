package mxstar.type;

abstract public class Type {
	public enum HyperTypes {
		VOID, INT, BOOL, STRING, CLASS, ARRAY, FUNCTION, NULL
	}

	HyperTypes hyperType;
	int varSize;

	public HyperTypes getHyperType() {
		return hyperType;
	}

	public int getVarSize() {
		return varSize;
	}

	public void setVarSize(int varSize) {
		this.varSize = varSize;
	}
}
