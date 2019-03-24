package mxstar.ir;

public class IRJump extends IRJumpInst {
	private BasicBlock targetBB;

	public IRJump(BasicBlock targetBB, BasicBlock parentBB) {
		super(parentBB);
		this.targetBB = targetBB;
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
	public void reloadRegLists() {

	}

	@Override
	public IRRegister getDefinedReg() {
		return null;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
