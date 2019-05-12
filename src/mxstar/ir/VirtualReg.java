package mxstar.ir;

public class VirtualReg extends IRRegister {
	private String name;
	private PhysicalReg forcedPhysicalReg = null;
	private boolean isTiny = false;
	private PhysicalReg color = null;

	public VirtualReg(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setForcedPhysicalReg(PhysicalReg forced) {
		forcedPhysicalReg = forced;
	}

	public PhysicalReg getForcedPhysicalReg() {
		return forcedPhysicalReg;
	}

	public void setTiny(boolean tiny) {
		isTiny = tiny;
	}

	public boolean isTiny() {
		return isTiny;
	}

	public void setColor(PhysicalReg color) {
		this.color = color;
	}

	public PhysicalReg getColor() {
		return color;
	}

	@Override
	public VirtualReg copy() {
		return new VirtualReg(name);
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
