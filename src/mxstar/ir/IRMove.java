package mxstar.ir;

import java.util.Map;

public class IRMove extends IRInstruction {
	IRRegister dest;
	RegValue src;

	public IRMove(IRRegister dest,RegValue src,BasicBlock parentBlock) {
		super(parentBlock);
		this.dest = dest;
		this.src = src;
		reloadRegLists();
	}

	public IRRegister getDest() {
		return dest;
	}

	public RegValue getSrc() {
		return src;
	}

	@Override
	public IRRegister getDefinedReg() {
		return dest;
	}

	@Override
	public void reloadRegLists() {
		usedRegisterList.clear();
		if(src instanceof IRRegister) usedRegisterList.add((IRRegister) src);

		usedRegValueList.clear();
		usedRegValueList.add(src);
	}

	@Override
	public void setUsedRegisterList(Map<IRRegister, IRRegister> renameMap) {
		if(src instanceof IRRegister) src = renameMap.get(src);
		reloadRegLists();
	}

	@Override
	public void setDefinedRegister(IRRegister register) {
		dest = register;
	}

	@Override
	public IRInstruction copyRename(Map<Object, Object> renameMap) {
		return new IRMove(
				(IRRegister) renameMap.getOrDefault(dest, dest),
				(RegValue) renameMap.getOrDefault(src, src),
				(BasicBlock) renameMap.getOrDefault(getParentBB(), getParentBB())
		);
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
