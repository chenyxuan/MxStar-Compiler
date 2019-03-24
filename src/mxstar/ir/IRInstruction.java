package mxstar.ir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

abstract public class IRInstruction {
	private IRInstruction prevInst = null, nextInst = null;

	private BasicBlock parentBB;

	public Set<VirtualReg> liveIn = new HashSet<>(), liveOut = new HashSet<>();
	protected List<IRRegister> usedRegisterList = new ArrayList<>();
	protected List<RegValue> usedRegValueList = new ArrayList<>();

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

	abstract public IRRegister getDefinedReg();

	abstract public void reloadRegLists();

	abstract public void accept(IRVisitor visitor);
}
