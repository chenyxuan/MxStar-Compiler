package mxstar.ir;

import java.util.Map;

public class IRJump extends IRJumpInst {
	private BasicBlock targetBB;

	public IRJump(BasicBlock targetBB, BasicBlock parentBB) {
		super(parentBB);
		this.targetBB = targetBB;
	}

	@Override
	public void reloadRegLists() {

	}

	@Override
	public void setUsedRegisterList(Map<IRRegister, IRRegister> renameMap) {

	}

	@Override
	public void setDefinedRegister(IRRegister register) {

	}

	@Override
	public IRInstruction copyRename(Map<Object, Object> renameMap) {
		return new IRJump(
				(BasicBlock) renameMap.getOrDefault(targetBB, targetBB),
				(BasicBlock) renameMap.getOrDefault(getParentBB(), getParentBB())
		);
	}

	public BasicBlock getTargetBB() {
		return targetBB;
	}

	@Override
	public void addedTo(BasicBlock block) {
		block.appendInst(this);
		block.addDestBB(targetBB);
	}

	@Override
	public void removedFrom(BasicBlock block) {
		block.removeDestBB();
		block.removeInst(this);
	}

	@Override
	public IRRegister getDefinedReg() {
		return null;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
