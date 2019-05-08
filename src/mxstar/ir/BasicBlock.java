package mxstar.ir;

import mxstar.ast.ForStmtNode;
import mxstar.ast.StmtNode;

import java.util.HashSet;

import static mxstar.ir.IRInstruction.join;

public class BasicBlock {
	private IRFunction function;
	private String name;

	private IRInstruction headInst = null, tailInst = null;
	private boolean escaped = false;

	private HashSet<BasicBlock> destBBSet = new HashSet<>();

	public ForStmtNode forNode = null;

	public void clear() {
		headInst = tailInst = null;
		escaped = false;
		destBBSet.clear();
	}

	public BasicBlock(IRFunction function, String name) {
		this.function = function;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public IRFunction getFunction() {
		return function;
	}

	public IRInstruction getHeadInst() {
		return headInst;
	}

	public IRInstruction getTailInst() {
		return tailInst;
	}

	public void setHeadInst(IRInstruction headInst) {
		this.headInst = headInst;
	}

	public void setTailInst(IRInstruction tailInst) {
		this.tailInst = tailInst;
	}

	public void appendInst(IRInstruction inst) {
		if(headInst == null) {
			headInst = tailInst = inst;
		}
		else {
			join(tailInst, inst);
			tailInst = inst;
		}
	}
	public void prependInst(IRInstruction inst, IRInstruction pInst) {
		if(inst == headInst) {
			headInst = pInst;
			join(pInst, inst);
		}
		else {
			join(inst.getPrevInst(), pInst);
			join(pInst, inst);
		}
	}
	public void insertInst(IRInstruction inst, IRInstruction nInst) {
		if(inst == tailInst) {
			tailInst = nInst;
			join(inst, nInst);
		}
		else {
			join(nInst, inst.getNextInst());
			join(inst, nInst);
		}
	}

	public void removeInst(IRInstruction inst) {
		if(tailInst == inst) {
			tailInst = tailInst.getPrevInst();
		}
		if(headInst == inst) {
			headInst = headInst.getNextInst();
		}
		join(inst.getPrevInst(), inst.getNextInst());
	}

	public void setJumpInst(IRJumpInst inst) {
		escaped = true;
		inst.addedTo(this);
	}

	public void removeJumpInst() {
		escaped = false;
		((IRJumpInst) tailInst).removedFrom(this);
	}

	public boolean isEscaped() {
		return escaped;
	}

	void addDestBB(BasicBlock block) {
		if(block != null) {
			destBBSet.add(block);
		}
	}

	void removeDestBB() {
		destBBSet.clear();
	}

	public HashSet<BasicBlock> getDestBBSet() {
		return destBBSet;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
