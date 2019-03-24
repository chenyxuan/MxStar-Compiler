package mxstar.ir;

public class IRHeapAlloc extends IRInstruction {
	private IRRegister dest;
	private RegValue allocSize;

	public IRHeapAlloc(IRRegister dest, RegValue allocSize,BasicBlock parentBB) {
		super(parentBB);
		this.dest = dest;
		this.allocSize = allocSize;
	}

	public IRRegister getDest() {
		return dest;
	}

	public RegValue getAllocSize() {
		return allocSize;
	}

	@Override
	public void reloadRegLists() {
		usedRegisterList.clear();
		if(allocSize instanceof IRRegister) usedRegisterList.add((IRRegister) allocSize);

		usedRegValueList.clear();
		usedRegValueList.add(allocSize);
	}

	@Override
	public IRRegister getDefinedReg() {
		return dest;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
