package mxstar.ir;

abstract public class IRJumpInst extends IRInstruction{
	public IRJumpInst(BasicBlock parentBB) { super(parentBB); }

	abstract public void addedTo(BasicBlock block);
	abstract public void removedFrom(BasicBlock block);
}
