package mxstar.ir;

public class PhysicalReg extends IRRegister {
	@Override
	public RegValue copy() {
		return null;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
