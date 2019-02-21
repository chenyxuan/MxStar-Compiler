package mxstar.ir;

public class IntImm extends RegValue {
	private int value;

	public IntImm(int value) {
		this.value = value;
	}

	@Override
	public IntImm copy() {
		return new IntImm(value);
	}

	public int getValue() {
		return value;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
