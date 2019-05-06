package mxstar.ir;

import mxstar.ast.BinaryExprNode;

import java.util.Map;

public class IRComparison extends IRInstruction {
	public enum Ops {
		GT, LT, GEQ, LEQ, EQ, NEQ
	}

	private Ops op;
	private IRRegister dest;
	private RegValue lhs, rhs;

	public IRComparison(Ops op, IRRegister dest, RegValue lhs, RegValue rhs, BasicBlock parentBB) {
		super(parentBB);
		this.op = op;
		this.dest = dest;
		this.lhs = lhs;
		this.rhs = rhs;
		reloadRegLists();
	}

	public RegValue getLhs() {
		return lhs;
	}

	public RegValue getRhs() {
		return rhs;
	}

	public Ops getOp() {
		return op;
	}

	public IRRegister getDest() {
		return dest;
	}


	@Override
	public IRRegister getDefinedReg() {
		return dest;
	}

	@Override
	public void setDefinedRegister(IRRegister register) {
		dest = register;
	}

	@Override
	public void reloadRegLists() {
		usedRegisterList.clear();
		if(lhs instanceof IRRegister) usedRegisterList.add((IRRegister) lhs);
		if(rhs instanceof IRRegister) usedRegisterList.add((IRRegister) rhs);

		usedRegValueList.clear();
		usedRegValueList.add(lhs);
		usedRegValueList.add(rhs);
	}

	@Override
	public void setUsedRegisterList(Map<IRRegister, IRRegister> renameMap) {
		if(lhs instanceof  IRRegister) lhs = renameMap.get(lhs);
		if(rhs instanceof  IRRegister) rhs = renameMap.get(rhs);
		reloadRegLists();
	}

	@Override
	public IRInstruction copyRename(Map<Object, Object> renameMap) {
		return new IRComparison(op,
				(IRRegister) renameMap.getOrDefault(dest, dest),
				(RegValue) renameMap.getOrDefault(lhs, lhs),
				(RegValue) renameMap.getOrDefault(rhs, rhs),
				(BasicBlock) renameMap.getOrDefault(getParentBB(), getParentBB())
		);
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}

	public static Ops trans(BinaryExprNode.Ops op) {
		Ops retOp = null;
		switch (op) {
			case GT:
				retOp = Ops.GT;
				break;
			case LT:
				retOp = Ops.LT;
				break;
			case GEQ:
				retOp = Ops.GEQ;
				break;
			case LEQ:
				retOp = Ops.LEQ;
				break;
			case EQ:
				retOp = Ops.EQ;
				break;
			case NEQ:
				retOp = Ops.NEQ;
				break;
			default:
				assert false;
		}
		return retOp;
	}
	public static Ops reverse(Ops op) {
		Ops retOp = null;
		switch (op) {
			case GT:
				retOp = Ops.LT;
				break;
			case LT:
				retOp = Ops.GT;
				break;
			case GEQ:
				retOp = Ops.LEQ;
				break;
			case LEQ:
				retOp = Ops.GEQ;
				break;
			case EQ:
				retOp = Ops.EQ;
				break;
			case NEQ:
				retOp = Ops.NEQ;
				break;
		}
		return retOp;
	}
	public static int getResult(Ops op,int x,int y) {
		switch (op) {
			case GT:
				return x > y ? 1 : 0;
			case LT:
				return x < y ? 1 : 0;
			case GEQ:
				return x >= y ? 1 : 0;
			case LEQ:
				return x <= y ? 1 : 0;
			case EQ:
				return x == y ? 1 : 0;
			case NEQ:
				return x != y ? 1 : 0;
		}
		return -1;
	}
}
