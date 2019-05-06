package mxstar.ir;

import java.util.List;
import java.util.Map;

public class IRLoad extends IRInstruction {
	private IRRegister dest;
	private RegValue addr;
	private int offset;

	public IRLoad(IRRegister dest, RegValue addr, int offset, BasicBlock parentBB) {
		super(parentBB);
		this.dest = dest;
		this.addr = addr;
		this.offset = offset;
		reloadRegLists();
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

	public void setAddr(RegValue addr) {
		this.addr = addr;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public IRRegister getDefinedReg() {
		return dest;
	}

	@Override
	public void reloadRegLists() {
		usedRegisterList.clear();
		if(addr instanceof IRRegister) usedRegisterList.add((IRRegister) addr);

		usedRegValueList.clear();
		usedRegValueList.add(addr);
	}

	@Override
	public void setUsedRegisterList(Map<IRRegister, IRRegister> renameMap) {
		if(addr instanceof IRRegister && !(addr instanceof StackSlot)) addr = renameMap.get(addr);
		reloadRegLists();
	}

	@Override
	public List<IRRegister> getUsedRegisterList() {
		return super.getUsedRegisterList();
	}

	@Override
	public IRInstruction copyRename(Map<Object, Object> renameMap) {
		return new IRLoad(
				(IRRegister) renameMap.getOrDefault(dest, dest),
				(RegValue) renameMap.getOrDefault(addr, addr),
				offset,
				(BasicBlock) renameMap.getOrDefault(getParentBB(), getParentBB())
		);
	}

	@Override
	public void setDefinedRegister(IRRegister register) {
		dest = register;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
