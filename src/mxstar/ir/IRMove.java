package mxstar.ir;

public class IRMove extends IRInstruction {
	IRRegister dest;
	RegValue src;

	public IRMove(IRRegister dest,RegValue src,BasicBlock parentBlock) {
		super(parentBlock);
		this.dest = dest;
		this.src = src;
	}

	public IRRegister getDest() {
		return dest;
	}

	public RegValue getSrc() {
		return src;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
