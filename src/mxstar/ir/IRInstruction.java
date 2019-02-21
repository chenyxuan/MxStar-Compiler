package mxstar.ir;

abstract public class IRInstruction {
	private IRInstruction prevInst = null, nextInst = null;

	private BasicBlock parentBB;

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

	abstract public void accept(IRVisitor visitor);

}
