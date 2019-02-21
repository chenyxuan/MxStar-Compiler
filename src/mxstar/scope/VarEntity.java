package mxstar.scope;

import mxstar.ast.VarDeclNode;
import mxstar.ir.IRRegister;
import mxstar.type.Type;

public class VarEntity extends Entity {
	private int addrOffset = 0;
	private IRRegister irRegister = null;
	private boolean isMember = false;

	public VarEntity(Type type, String name) {
		super(type, name);
	}

	public VarEntity(VarDeclNode node) {
		this(node.getType().getType(), node.getName());
	}

	public void setAddrOffset(int addrOffset) {
		this.addrOffset = addrOffset;
	}

	public int getAddrOffset() {
		return addrOffset;
	}

	public void setIRRegister(IRRegister irRegister) {
		this.irRegister = irRegister;
	}

	public IRRegister getIRRegister() {
		return irRegister;
	}

	public void setMember(boolean member) {
		isMember = member;
	}

	public boolean isMember() {
		return isMember;
	}
}
