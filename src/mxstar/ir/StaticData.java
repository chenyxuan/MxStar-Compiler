package mxstar.ir;

abstract public class StaticData extends IRRegister {
	private String name;
	private int size;

	public StaticData(String name, int size) {
		this.name = name;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

	@Override
	public StaticData copy() {
		return this;
	}

	abstract public void accept(IRVisitor visitor);
}
