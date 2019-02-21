package mxstar.ir;

public class IRReturn extends IRJumpInst {
	private RegValue retValue;

	public IRReturn(RegValue retValue, BasicBlock parentBB) {
		super(parentBB);
		this.retValue = retValue;
	}

	public RegValue getRetValue() {
		return retValue;
	}


	@Override
	public void addedTo(BasicBlock block) {
		block.appendInst(this);
		block.getFunction().getIRReturns().add(this);
	}

	@Override
	public void removedFrom(BasicBlock block) {
		block.getFunction().getIRReturns().remove(this);
		block.removeInst(this);
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
