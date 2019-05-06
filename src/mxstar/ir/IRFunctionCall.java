package mxstar.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IRFunctionCall extends IRInstruction {
	private IRFunction func;
	private IRRegister dest;
	private List<RegValue> args;

	public IRFunctionCall(IRFunction func, IRRegister dest, List<RegValue> args, BasicBlock parentBB) {
		super(parentBB);
		this.func = func;
		this.dest = dest;
		this.args = args;
		reloadRegLists();
	}

	public IRRegister getDest() {
		return dest;
	}

	public IRFunction getFunc() {
		return func;
	}

	public List<RegValue> getArgs() {
		return args;
	}

	@Override
	public void reloadRegLists() {
		usedRegisterList.clear();
		usedRegValueList.clear();
		for(RegValue arg : args) {
			if(arg instanceof IRRegister) usedRegisterList.add((IRRegister) arg);
			usedRegValueList.add(arg);
		}
	}

	@Override
	public void setUsedRegisterList(Map<IRRegister, IRRegister> renameMap) {
		for (int i = 0; i < args.size(); i++) {
			if (args.get(i) instanceof IRRegister) {
				IRRegister arg = (IRRegister) args.get(i);
				args.set(i, renameMap.get(arg));
			}
		}
		reloadRegLists();
	}

	@Override
	public IRRegister getDefinedReg() {
		return dest;
	}

	@Override
	public void setDefinedRegister(IRRegister register) {
		dest = register;
	}

	@Override
	public IRInstruction copyRename(Map<Object, Object> renameMap) {
		List<RegValue> copyArgs = new ArrayList<>();
		for (RegValue arg : args) {
			copyArgs.add((RegValue) renameMap.getOrDefault(arg, arg));
		}
		IRRegister copyDest = (IRRegister) renameMap.getOrDefault(dest, dest);
		BasicBlock copyBB = (BasicBlock) renameMap.getOrDefault(getParentBB(), getParentBB());

		return new IRFunctionCall(func, copyDest, copyArgs, copyBB);
	}

	public void accept(IRVisitor visitor) {
		visitor.visit(this);
	}
}
