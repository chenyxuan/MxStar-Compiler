package mxstar.ir;

public class IRStore extends IRInstruction {
	private RegValue addr;
	private int offset;
	private RegValue value;

	public IRStore(RegValue addr, int offset, RegValue value, BasicBlock parentBB) {
		super(parentBB);
		this.addr = addr;
		this.offset = offset;
		this.value = value;
	}

	public RegValue getAddr() {
		return addr;
	}

	public int getOffset() {
		return offset;
	}

	public RegValue getValue() {
		return value;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
