package mxstar.ir;

public interface IRVisitor {
	void visit(BasicBlock node);
	void visit(IntImm node);
	void visit(IRBinaryOp node);
	void visit(IRBranch node);
	void visit(IRComp node);
	void visit(IRFuncCall node);
	void visit(IRFunction node);
	void visit(IRHeapAlloc node);
	void visit(IRJump node);
	void visit(IRLoad node);
	void visit(IRMove node);
	void visit(IRReturn node);
	void visit(IRRoot node);
	void visit(IRStore node);
	void visit(IRUnaryOp node);
	void visit(StaticVar node);
	void visit(StaticString node);
	void visit(VirtualReg node);
	void visit(PhysicalReg node);
}
