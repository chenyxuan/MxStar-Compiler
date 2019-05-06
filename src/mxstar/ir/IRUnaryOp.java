package mxstar.ir;

import java.util.Map;

public class IRUnaryOp extends IRInstruction {
	public enum Ops {
		POS, NEG, BIT_NOT
	}

	private Ops op;
	private IRRegister dest;
	private RegValue rhs;

	public IRUnaryOp(Ops op, IRRegister dest, RegValue rhs, BasicBlock parentBB) {
		super(parentBB);
		this.op = op;
		this.dest = dest;
		this.rhs = rhs;
		reloadRegLists();
	}

	public IRRegister getDest() {
		return dest;
	}

	public Ops getOp() {
		return op;
	}

	public RegValue getRhs() {
		return rhs;
	}

	@Override
	public IRRegister getDefinedReg() {
		return dest;
	}

	@Override
	public void reloadRegLists() {
		usedRegisterList.clear();
		if(rhs instanceof IRRegister) usedRegisterList.add((IRRegister) rhs);
		usedRegValueList.add(rhs);
	}

	@Override
	public IRInstruction copyRename(Map<Object, Object> renameMap) {
		return new IRUnaryOp(
				op,
				(IRRegister) renameMap.getOrDefault(dest, dest),
				(RegValue) renameMap.getOrDefault(rhs, rhs),
				(BasicBlock) renameMap.getOrDefault(getParentBB(), getParentBB())
		);
	}

	@Override
	public void setUsedRegisterList(Map<IRRegister, IRRegister> renameMap) {
		if(rhs instanceof IRRegister) rhs = renameMap.get(rhs);
		reloadRegLists();
	}

	@Override
	public void setDefinedRegister(IRRegister register) {
		dest = register;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
