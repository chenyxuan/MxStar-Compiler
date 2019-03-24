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

	@Override
	public IRRegister getDefinedReg() {
		return null;
	}

	@Override
	public void reloadRegLists() {
		usedRegisterList.clear();
		if(addr instanceof IRRegister && !(addr instanceof StackSlot)) usedRegisterList.add((IRRegister) addr);
		if(value instanceof  IRRegister) usedRegisterList.add((IRRegister) value);
		usedRegValueList.add(addr);
		usedRegValueList.add(value);
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
