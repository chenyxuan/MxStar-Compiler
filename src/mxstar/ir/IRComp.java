package mxstar.ir;

import mxstar.ast.BinaryExprNode;

public class IRComp extends IRInstruction {
	public enum Ops {
		GT, LT, GEQ, LEQ, EQ, NEQ
	}

	private Ops op;
	private IRRegister dest;
	private RegValue lhs, rhs;

	public IRComp(Ops op, IRRegister dest, RegValue lhs, RegValue rhs, BasicBlock parentBB) {
		super(parentBB);
		this.op = op;
		this.dest = dest;
		this.lhs = lhs;
		this.rhs = rhs;
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

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
