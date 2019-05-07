package mxstar.ir;


import mxstar.ast.BinaryExprNode;

import java.util.List;
import java.util.Map;

public class IRBinaryOp extends IRInstruction {
	public enum Ops {
		ADD, SUB, MUL, DIV, MOD,
		SHL, SHR,
		BIT_AND, BIT_OR, BIT_XOR
	}

	private Ops op;
	private IRRegister dest;
	private RegValue lhs, rhs;

	public IRBinaryOp(Ops op, IRRegister dest, RegValue lhs, RegValue rhs, BasicBlock parentBB) {
		super(parentBB);
		this.op = op;
		this.dest = dest;
		this.lhs = lhs;
		this.rhs = rhs;
		reloadRegLists();
	}

	public Ops getOp() {
		return op;
	}

	public IRRegister getDest() {
		return dest;
	}

	public RegValue getRhs() {
		return rhs;
	}

	public RegValue getLhs() {
		return lhs;
	}

	public void setLhs(RegValue lhs) {
		this.lhs = lhs;
		reloadRegLists();
	}

	public void setRhs(RegValue rhs) {
		this.rhs = rhs;
		reloadRegLists();
	}

	public boolean isCommutativeOp() {
		return op == Ops.ADD || op == Ops.MUL || op == Ops.BIT_AND || op == Ops.BIT_OR || op == Ops.BIT_XOR;
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
		if(lhs instanceof IRRegister) lhs = renameMap.get(lhs);
		if(rhs instanceof IRRegister) rhs = renameMap.get(rhs);
		reloadRegLists();
	}

	@Override
	public IRBinaryOp copyRename(Map<Object, Object> renameMap) {
		return new IRBinaryOp(op,
				(IRRegister) renameMap.getOrDefault(dest, dest),
				(RegValue) renameMap.getOrDefault(lhs, lhs),
				(RegValue) renameMap.getOrDefault(rhs, rhs),
				(BasicBlock) renameMap.getOrDefault(getParentBB(), getParentBB()));
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}


	public static Ops trans(BinaryExprNode.Ops op) {
		Ops retOp = null;
		switch (op) {
			case ADD:
				retOp = Ops.ADD;
				break;
			case SUB:
				retOp = Ops.SUB;
				break;
			case MUL:
				retOp = Ops.MUL;
				break;
			case DIV:
				retOp = Ops.DIV;
				break;
			case MOD:
				retOp = Ops.MOD;
				break;
			case SHL:
				retOp = Ops.SHL;
				break;
			case SHR:
				retOp = Ops.SHR;
				break;
			case BIT_AND:
				retOp = Ops.BIT_AND;
				break;
			case BIT_OR:
				retOp = Ops.BIT_OR;
				break;
			case BIT_XOR:
				retOp = Ops.BIT_XOR;
			default:
				assert false;
		}
		return retOp;
	}
	public static int getResult(BinaryExprNode.Ops op,int x,int y) {
		switch (op) {
			case ADD:
				return x + y;
			case SUB:
				return x - y;
			case MUL:
				return x * y;
			case DIV:
				return y != 0 ? x / y : -1;
			case MOD:
				return y != 0 ? x % y : -1;
			case SHL:
				return y >= 0 ? x >> y : -1;
			case SHR:
				return y >= 0 ? x << y : -1;
			case BIT_AND:
				return x & y;
			case BIT_OR:
				return x | y;
			case BIT_XOR:
				return x ^ y;
		}
		return -1;
	}
}
