package mxstar.ir;

public class IRLoad extends IRInstruction {
	private IRRegister dest;
	private RegValue addr;
	private int offset;

	public IRLoad(IRRegister dest, RegValue addr, int offset, BasicBlock parentBB) {
		super(parentBB);
		this.dest = dest;
		this.addr = addr;
		this.offset = offset;
	}

	public IRRegister getDest() {
		return dest;
	}

	public int getOffset() {
		return offset;
	}

	public RegValue getAddr() {
		return addr;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
