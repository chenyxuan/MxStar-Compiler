package mxstar.ir;

import mxstar.symbol.scope.FuncEntity;
import static mxstar.utility.GlobalSymbols.*;

import java.util.*;

public class IRFunction {
	private FuncEntity funcEntity;
	private String name;

	private BasicBlock beginBB = null;
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

	public boolean isTrivial() {
		return isTrivial;
	}

	public void setTrivial(boolean trivial) {
		isTrivial = trivial;
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

	public BasicBlock getBeginBB() {
		if(beginBB != null) return beginBB;
		return beginBB = new BasicBlock(this, name + FUNC_ENTRY);
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
			dfs(getBeginBB(), new HashSet<>());
		}
		return allBB;
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
