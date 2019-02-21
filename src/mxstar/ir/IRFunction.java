package mxstar.ir;

import mxstar.scope.FuncEntity;
import static mxstar.utility.GlobalSymbols.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IRFunction {
	private FuncEntity funcEntity;
	private String name;

	private BasicBlock beginBB = null, endBB = null;
	private List<IRReturn> irReturns = new ArrayList<>();
	private List<VirtualReg> paraRegs = new ArrayList<>();
	private List<BasicBlock> allBB = null;

	public IRFunction(FuncEntity funcEntity) {
		this.funcEntity = funcEntity;
		this.name = (funcEntity.isMember() ? (funcEntity.getClassEntity().getName() + '.') : "") + funcEntity.getName();
	}

	public static String parseName(FuncEntity funcEntity) {
		return (funcEntity.isMember() ? (funcEntity.getClassEntity().getName() + '.') : "") + funcEntity.getName();
	}

	public boolean isMain() {
		return name.equals( "main");
	}

	public BasicBlock getBeginBB() {
		if(beginBB != null) return beginBB;
		return beginBB = new BasicBlock(this, name + FUNC_ENTRY);
	}

	public BasicBlock getEndBB() {
		return endBB;
	}

	public void setEndBB(BasicBlock endBB) {
		this.endBB = endBB;
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

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
