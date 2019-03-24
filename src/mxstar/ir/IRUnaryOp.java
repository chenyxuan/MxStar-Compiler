package mxstar.ir;

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

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
