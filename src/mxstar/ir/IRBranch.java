package mxstar.ir;

import mxstar.ast.ExprNode;

import java.util.Map;

public class IRBranch extends IRJumpInst {
	private RegValue cond;
	private BasicBlock thenBB, elseBB;

	public IRBranch(RegValue cond, BasicBlock thenBB, BasicBlock elseBB, BasicBlock parentBB) {
		super(parentBB);
		this.cond = cond;
		this.thenBB = thenBB;
		this.elseBB = elseBB;
		reloadRegLists();
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

	public void setThenBB(BasicBlock thenBB) {
		this.thenBB = thenBB;
	}

	public void setElseBB(BasicBlock elseBB) {
		this.elseBB = elseBB;
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

	@Override
	public void reloadRegLists() {
		usedRegisterList.clear();
		if(cond instanceof IRRegister) usedRegisterList.add((IRRegister) cond);

		usedRegValueList.clear();
		usedRegValueList.add(cond);
	}

	@Override
	public IRRegister getDefinedReg() {
		return null;
	}

	@Override
	public void setDefinedRegister(IRRegister register) {
	}

	@Override
	public void setUsedRegisterList(Map<IRRegister, IRRegister> renameMap) {
		if(cond instanceof IRRegister) cond = renameMap.get(cond);
		reloadRegLists();
	}

	@Override
	public IRBranch copyRename(Map<Object, Object> renameMap) {
		return new IRBranch((RegValue) renameMap.getOrDefault(cond, cond),
				(BasicBlock) renameMap.getOrDefault(thenBB, thenBB),
				(BasicBlock) renameMap.getOrDefault(elseBB, elseBB),
				(BasicBlock) renameMap.getOrDefault(getParentBB(), getParentBB())
				);
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
