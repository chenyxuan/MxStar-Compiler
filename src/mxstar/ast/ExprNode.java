package mxstar.ast;

import mxstar.ir.BasicBlock;
import mxstar.ir.IRFunction;
import mxstar.ir.RegValue;
import mxstar.scope.FuncEntity;
import mxstar.type.BoolType;
import mxstar.type.Type;

abstract public class ExprNode extends StmtNode {
	private Type type = null;
	private boolean leftValue = false;
	private RegValue regValue = null;
	private BasicBlock trueBB = null, falseBB = null;

	private RegValue addrValue = null;
	private int addrOffset = 0;

	public boolean isBoolExpr() {
		return type instanceof BoolType && !(this instanceof BoolConstNode);
	}

	public void initFlowBB(IRFunction irFunction) {
		trueBB = new BasicBlock(irFunction, null);
		falseBB = new BasicBlock(irFunction, null);
	}

	public boolean hasFlowBB() {
		return trueBB != null && falseBB != null;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setLeftValue(boolean leftValue) {
		this.leftValue = leftValue;
	}

	public boolean isLeftValue() {
		return leftValue;
	}

	public void setRegValue(RegValue regValue) {
		this.regValue = regValue;
	}

	public RegValue getRegValue() {
		return regValue;
	}

	public void setTrueBB(BasicBlock trueBB) {
		this.trueBB = trueBB;
	}

	public BasicBlock getTrueBB() {
		return trueBB;
	}

	public void setFalseBB(BasicBlock falseBB) {
		this.falseBB = falseBB;
	}

	public BasicBlock getFalseBB() {
		return falseBB;
	}

	public void setAddrValue(RegValue addrValue) {
		this.addrValue = addrValue;
	}

	public void setAddrOffset(int addrOffset) {
		this.addrOffset = addrOffset;
	}

	public RegValue getAddrValue() {
		return addrValue;
	}

	public int getAddrOffset() {
		return addrOffset;
	}
}
