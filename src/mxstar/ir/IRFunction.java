package mxstar.ir;

import mxstar.symbol.scope.FuncEntity;
import static mxstar.utility.GlobalSymbols.*;

import java.util.*;

public class IRFunction {
	private FuncEntity funcEntity;
	private String name;

	private BasicBlock beginBB = null;
	private BasicBlock endBB = null;
	private List<IRReturn> irReturns = new ArrayList<>();
	private List<VirtualReg> paraRegs = new ArrayList<>();
	private List<BasicBlock> allBB = null;

	private boolean recursiveCall = false;

	public Set<IRFunction> calleeSet = new HashSet<>();
	public Set<IRFunction> recursiveCalleeSet = new HashSet<>();

	public List<StackSlot> stackSlots = new ArrayList<>();
	public Map<VirtualReg, StackSlot> slotMap = new HashMap<>();

	public Set<PhysicalReg> usedPhysicalGeneralRegs = new HashSet<>();
	private boolean builtIn = false;
	private boolean isTrivial = false;
	private boolean isMember = false;
	private boolean isOrphan = false;

	public void setOrphan(boolean orphan) {
		isOrphan = orphan;
	}

	public boolean isOrphan() {
		return isOrphan;
	}

	public boolean isTrivial() {
		return isTrivial;
	}

	public void setTrivial(boolean trivial) {
		isTrivial = trivial;
	}

	public void setMember(boolean member) {
		isMember = member;
	}

	public boolean isMember() {
		return isMember;
	}

	public IRFunction() {
		funcEntity = null;
		name = "unknown";
	}

	public IRFunction(FuncEntity funcEntity) {
		this.funcEntity = funcEntity;
		this.name = parseName(funcEntity);
	}

	public static String parseName(FuncEntity funcEntity) {
		return (funcEntity.isMember() ? (funcEntity.getClassEntity().getName() + "_") : "") + funcEntity.getName();
	}

	public boolean isMain() {
		return name.equals( "main");
	}

	public void setParaRegs(List<VirtualReg> paraRegs) {
		this.paraRegs = paraRegs;
	}

	public BasicBlock getBeginBB() {
		if(beginBB != null) return beginBB;
		return beginBB = new BasicBlock(this, name + FUNC_ENTRY);
	}

	public void setBeginBB(BasicBlock beginBB) {
		this.beginBB = beginBB;
	}

	public BasicBlock getEndBB() {
		return endBB;
	}

	public void setEndBB(BasicBlock endBB) {
		this.endBB = endBB;
	}

	public void updateCalleeSet() {
		calleeSet.clear();
		for (BasicBlock bb : getAllBB()) {
			for (IRInstruction inst = bb.getHeadInst(); inst != null; inst = inst.getNextInst()) {
				if (inst instanceof IRFunctionCall) {
					calleeSet.add(((IRFunctionCall) inst).getFunc());
					if(((IRFunctionCall) inst).getFunc() == null) throw new Error("?");
				}
			}
		}
	}

	public void setRecursiveCall(boolean recursiveCall) {
		this.recursiveCall = recursiveCall;
	}

	public boolean isRecursiveCall() {
		return recursiveCall;
	}

	public FuncEntity getFuncEntity() {
		return funcEntity;
	}

	public String getName() {
		return name;
	}

	public List<IRReturn> getIRReturns() {
		return irReturns;
	}

	public List<VirtualReg> getParaRegs() {
		return paraRegs;
	}

	private void dfs(BasicBlock node, Set<BasicBlock> visited) {
		if(visited.contains(node)) return;
		visited.add(node);
		allBB.add(node);

		for(BasicBlock dest : node.getDestBBSet()) {
			dfs(dest, visited);
		}
	}
	public List<BasicBlock> getAllBB() {
		if(allBB == null) {
			allBB = new ArrayList<>();
			reAllBB();
		}
		return allBB;
	}

	public void reAllBB()
	{
		allBB.clear();
		dfs(getBeginBB(), new HashSet<>());
	}

	public void setBuiltIn(boolean builtIn) {
		this.builtIn = builtIn;
	}

	public boolean isBuiltIn() {
		return builtIn;
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
