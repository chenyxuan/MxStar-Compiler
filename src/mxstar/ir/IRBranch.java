package mxstar.ir;

import mxstar.ast.ExprNode;

public class IRBranch extends IRJumpInst {
	private RegValue cond;
	private BasicBlock thenBB, elseBB;

	public IRBranch(RegValue cond, BasicBlock thenBB, BasicBlock elseBB, BasicBlock parentBB) {
		super(parentBB);
		this.cond = cond;
		this.thenBB = thenBB;
		this.elseBB = elseBB;
	}
	public IRBranch(ExprNode node, BasicBlock parentBB) {
		this(node.getRegValue(), node.getTrueBB(), node.getFalseBB(), parentBB);
	}

	public RegValue getCond() {
		return cond;
	}

	public BasicBlock getThenBB() {
		return thenBB;
	}

	public BasicBlock getElseBB() {
		return elseBB;
	}

	@Override
	public void addedTo(BasicBlock block) {
		block.appendInst(this);
		block.addDestBB(thenBB);
		block.addDestBB(elseBB);
	}

	@Override
	public void removedFrom(BasicBlock block) {
		block.removeDestBB();
		block.removeInst(this);
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
