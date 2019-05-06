package mxstar.ir;

import java.util.Map;

public class IRStore extends IRInstruction {
	private RegValue addr;
	private int offset;
	private RegValue value;

	public IRStore(RegValue addr, int offset, RegValue value, BasicBlock parentBB) {
		super(parentBB);
		this.addr = addr;
		this.offset = offset;
		this.value = value;
		reloadRegLists();
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

	public void setAddr(RegValue addr) {
		this.addr = addr;
	}

	public void setOffset(int offset) {
		this.offset = offset;
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

	@Override
	public IRInstruction copyRename(Map<Object, Object> renameMap) {
		return new IRStore(
				(RegValue) renameMap.getOrDefault(addr, addr),
				offset,
				(RegValue) renameMap.getOrDefault(value, value),
				(BasicBlock) renameMap.getOrDefault(getParentBB(), getParentBB())
		);
	}

	@Override
	public void setDefinedRegister(IRRegister register) {

	}

	@Override
	public void setUsedRegisterList(Map<IRRegister, IRRegister> renameMap) {
		if(addr instanceof IRRegister && !(addr instanceof StackSlot)) addr = renameMap.get(addr);
		if(value instanceof IRRegister) value = renameMap.get(value);
		reloadRegLists();
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
