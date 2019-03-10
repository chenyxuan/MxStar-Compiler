package mxstar.frontend;

import mxstar.ir.*;

import java.io.PrintStream;
import java.util.*;

public class IRPrinter implements IRVisitor {
	private PrintStream out;

	public IRPrinter(PrintStream out) {
		this.out = out;
	}

	private Map<BasicBlock, String> bbMap = new HashMap<>();
	private Map<VirtualReg, String> vregMap = new HashMap<>();
	private Map<StaticData, String> staticDataMap = new HashMap<>();

	private Map<String, Integer> bbCnt = new HashMap<>();
	private Map<String, Integer> vregCnt = new HashMap<>();
	private Map<String, Integer> staticDataCnt = new HashMap<>();

	private Set<BasicBlock> bbVisited = new HashSet<>();

	private boolean isStaticDef;

	private String genID(String name, Map<String, Integer> cnt) {
		int cntName = cnt.getOrDefault(name, 0) + 1;
		cnt.put(name, cntName);
		if (cntName == 1) return name;
		return name + "_" + cntName;
	}

	private String getBBID(BasicBlock bb) {
		String id = bbMap.get(bb);
		if (id == null) {
			if (bb.getName() == null) {
				id = genID("bb", bbCnt);
			} else {
				id = genID(bb.getName(), bbCnt);
			}
			bbMap.put(bb, id);
		}
		return id;
	}

	private String getVRegID(VirtualReg vreg) {
		String id = vregMap.get(vreg);
		if (id == null) {
			if (vreg.getName() == null) {
				id = genID("vreg", vregCnt);
			} else {
				id = genID(vreg.getName(), vregCnt);
			}
			vregMap.put(vreg, id);
		}
		return id;
	}

	private String getStaticDataID(StaticData data) {
		String id = staticDataMap.get(data);
		if (id == null) {
			if (data.getName() == null) {
				id = genID("staticData", staticDataCnt);
			} else {
				id = genID(data.getName(), staticDataCnt);
			}
			staticDataMap.put(data, id);
		}
		return id;
	}

	@Override
	public void visit(IRRoot node) {
		// Static Data
		isStaticDef = true;
		for (StaticData staticData : node.getStaticDataList()) {
			staticData.accept(this);
		}
		isStaticDef = false;
		out.println();

//		out.println(node.getFunctionList().size());
		for (IRFunction func : node.getFunctionList()) {
			func.accept(this);
		}
	}

	@Override
	public void visit(IRFunction node) {
		vregMap = new IdentityHashMap<>();
		vregCnt = new HashMap<>();
		out.printf("func %s ", node.getName());
		for (VirtualReg paraVReg : node.getParaRegs()) {
			out.printf("$%s ", getVRegID(paraVReg));
		}
		out.println("{");
		for (BasicBlock bb : node.getAllBB()) {
			bb.accept(this);
		}
		out.println("}\n");
	}

	@Override
	public void visit(BasicBlock node) {
		if (bbVisited.contains(node)) return;
		bbVisited.add(node);
		out.println("%" + getBBID(node) + ":");
		for (IRInstruction inst = node.getHeadInst(); inst != null; inst = inst.getNextInst()) {
			inst.accept(this);
		}
	}

	@Override
	public void visit(IRBranch node) {
		out.print("    br ");
		node.getCond().accept(this);
		out.println(" %" + getBBID(node.getThenBB()) + " %" + getBBID(node.getElseBB()));
		out.println();
	}

	@Override
	public void visit(IRJump node) {
		out.printf("    jump %%%s\n\n", getBBID(node.getTargetBB()));
	}

	@Override
	public void visit(IRReturn node) {
		out.print("    ret ");
		if (node.getRetValue() != null) {
			node.getRetValue().accept(this);
		} else {
			out.print("0");
		}
		out.println();
		out.println();
	}

	@Override
	public void visit(IRUnaryOp node) {
		out.print("    ");
		String op = null;
		switch (node.getOp()) {
			case NEG:
				op = "neg";
				break;
			case BIT_NOT:
				op = "not";
				break;
		}
		node.getDest().accept(this);
		out.printf(" = %s ", op);
		node.getRhs().accept(this);
		out.println();
	}

	@Override
	public void visit(IRBinaryOp node) {
		out.print("    ");
		String op = null;
		switch (node.getOp()) {
			case ADD:
				op = "add";
				break;
			case SUB:
				op = "sub";
				break;
			case MUL:
				op = "mul";
				break;
			case DIV:
				op = "div";
				break;
			case MOD:
				op = "rem";
				break;
			case SHL:
				op = "shl";
				break;
			case SHR:
				op = "shr";
				break;
			case BIT_AND:
				op = "and";
				break;
			case BIT_OR:
				op = "or";
				break;
			case BIT_XOR:
				op = "xor";
				break;
		}
		node.getDest().accept(this);
		out.printf(" = %s ", op);
		node.getLhs().accept(this);
		out.print(' ');
		node.getRhs().accept(this);
		out.println();
	}

	@Override
	public void visit(IRComp node) {
		out.print("    ");
		String op = null;
		switch (node.getOp()) {
			case EQ:
				op = "seq";
				break;
			case NEQ:
				op = "sne";
				break;
			case GT:
				op = "sgt";
				break;
			case GEQ:
				op = "sge";
				break;
			case LT:
				op = "slt";
				break;
			case LEQ:
				op = "sle";
				break;
		}
		node.getDest().accept(this);
		out.printf(" = %s ", op);
		node.getLhs().accept(this);
		out.print(" ");
		node.getRhs().accept(this);
		out.println();
	}

	@Override
	public void visit(IRMove node) {
		out.print("    ");
		node.getDest().accept(this);
		out.print(" = move ");
		node.getSrc().accept(this);
		out.println();
	}

	@Override
	public void visit(IRLoad node) {
		out.print("    ");
		node.getDest().accept(this);
		out.print(" = load ");
		node.getAddr().accept(this);
		out.println(" " + node.getOffset());
	}

	@Override
	public void visit(IRStore node) {
		out.print("    store ");
		node.getAddr().accept(this);
		out.print(" " + node.getOffset() + " ");
		node.getValue().accept(this);
		out.println();
	}

	@Override
	public void visit(IRFuncCall node) {
		out.print("    ");
		if (node.getDest() != null) {
			node.getDest().accept(this);
			out.print(" = ");
		}
		out.printf("call %s ", node.getFunc().getName());
		for (RegValue arg : node.getArgs()) {
			arg.accept(this);
			out.print(" ");
		}
		out.println();
	}

	@Override
	public void visit(IRHeapAlloc node) {
		out.print("    ");
		node.getDest().accept(this);
		out.print(" = alloc ");
		node.getAllocSize().accept(this);
		out.println();
	}

	@Override
	public void visit(VirtualReg node) {
		out.print("$" + getVRegID(node));
	}

	@Override
	public void visit(IntImm node) {
		out.print(node.getValue());
	}

	@Override
	public void visit(StaticVar node) {
		if (isStaticDef) out.printf("space @%s %d\n", getStaticDataID(node), node.getSize());
		else out.print("@" + getStaticDataID(node));
	}

	@Override
	public void visit(StaticStr node) {
		if (isStaticDef) out.printf("value @%s %s\n", getStaticDataID(node), node.getValue());
		else out.print("@" + getStaticDataID(node));
	}

	@Override
	public void visit(PhysicalReg node) {
		System.err.println("Can't visit PhysicalReg in IRPrinter");
		System.exit(1);
	}
}