package mxstar.ir;


import mxstar.ast.BinaryExprNode;

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

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
