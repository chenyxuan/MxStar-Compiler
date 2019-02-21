package mxstar.ir;

abstract public class RegValue {
	abstract public RegValue copy();
	abstract public void accept(IRVisitor visitor);
}
