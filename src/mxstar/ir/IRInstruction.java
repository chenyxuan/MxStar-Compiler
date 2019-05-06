package mxstar.ir;

import java.util.*;

abstract public class IRInstruction {
	private IRInstruction prevInst = null, nextInst = null;

	private BasicBlock parentBB;

	public Set<VirtualReg> liveIn = new HashSet<>(), liveOut = new HashSet<>();
	protected List<IRRegister> usedRegisterList = new ArrayList<>();
	protected List<RegValue> usedRegValueList = new ArrayList<>();

	private boolean removed = false;

	public IRInstruction(BasicBlock parentBB) {
		this.parentBB = parentBB;
	}

	public BasicBlock getParentBB() {
		return parentBB;
	}

	public static void join(IRInstruction p, IRInstruction q) {
		if (p != null) p.setNextInst(q);
		if (q != null) q.setPrevInst(p);
	}

	public void prependInst(IRInstruction pInst) {
		getParentBB().prependInst(this, pInst);
	}

	public void appendInst(IRInstruction pInst) {
		getParentBB().insertInst(this, pInst);
	}

	public void remove() {
		assert !removed;
		removed = true;
		if (prevInst != null) prevInst.setNextInst(nextInst);
		if (nextInst != null) nextInst.setPrevInst(prevInst);
		if (this instanceof IRJumpInst) {
			parentBB.removeJumpInst();
		}
		if (this == parentBB.getHeadInst()) parentBB.setHeadInst(nextInst);
		if (this == parentBB.getTailInst()) parentBB.setTailInst(prevInst);
	}
	public void setNextInst(IRInstruction nextInst) {
		this.nextInst = nextInst;
	}

	public IRInstruction getNextInst() {
		return nextInst;
	}

	public void setPrevInst(IRInstruction prevInst) {
		this.prevInst = prevInst;
	}

	public IRInstruction getPrevInst() {
		return prevInst;
	}

	public List<IRRegister> getUsedRegisterList() {
		return usedRegisterList;
	}

	public List<RegValue> getUsedRegValueList() {
		return usedRegValueList;
	}

	abstract public void setDefinedRegister(IRRegister register);

	abstract public IRRegister getDefinedReg();

	abstract public void reloadRegLists();

	abstract public void setUsedRegisterList(Map<IRRegister, IRRegister> renameMap);

	abstract public void accept(IRVisitor visitor);

	abstract public IRInstruction copyRename(Map<Object, Object> renameMap);

}
