package mxstar.ir;

import java.util.Map;

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

	@Override
	public void setDefinedRegister(IRRegister register) {
		dest = register;
	}

	@Override
	public void setUsedRegisterList(Map<IRRegister, IRRegister> renameMap) {
		if(allocSize instanceof IRRegister) allocSize = renameMap.get(allocSize);
		reloadRegLists();
	}

	@Override
	public IRInstruction copyRename(Map<Object, Object> renameMap) {
		return new IRHeapAlloc(
				(IRRegister) renameMap.getOrDefault(dest, dest),
				(RegValue) renameMap.getOrDefault(allocSize, allocSize),
				(BasicBlock) renameMap.getOrDefault(getParentBB(), getParentBB())
		);
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
