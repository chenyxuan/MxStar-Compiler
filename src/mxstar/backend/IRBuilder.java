package mxstar.backend;

import mxstar.ast.*;
import mxstar.frontend.SemanticAnalyser;
import mxstar.ir.*;
import mxstar.scope.*;
import mxstar.type.*;

import java.util.ArrayList;
import java.util.List;

import static mxstar.utility.GlobalSymbols.*;

public class IRBuilder extends ASTBaseVisitor {
	private Scope globalScope;

	private Scope currentScope = null;
	private IRRoot ir = new IRRoot();
	private IRFunction currentFunction = null;
	private BasicBlock currentBB = null;
	private boolean isParaDecl = false;

	private BasicBlock currentLoopStepBB = null;
	private BasicBlock currentLoopAfterBB = null;
	private boolean wantAddr = false;

	private List<GlobalInit> globalInits = new ArrayList<>();

	public IRRoot getIR() {
		return ir;
	}

	public IRBuilder(SemanticAnalyser analyser) {
		this.globalScope = analyser.getGlobalScope();
		ir.addBuiltInFunctions(analyser.getBuiltInFuncEntity());
	}

	@Override
	public void visit(ASTRootNode node) {
		currentScope = globalScope;
		ir.addFunction(new IRFunction(new FuncEntity(VoidType.getInstance(), INIT_STATIC_VAR, new ArrayList<>(), null)));
		super.visit(node);
		currentFunction = ir.getFunction(INIT_STATIC_VAR);
		currentBB = currentFunction.getBeginBB();
		for(GlobalInit e : globalInits) {
			if(e.getSrc().isBoolExpr()) {
				e.getSrc().initFlowBB(currentFunction);
			}
			e.getSrc().accept(this);
			IRAssign(e.getDest(), 0, e.getSrc(), false);
		}
		currentBB.setJumpInst(new IRReturn(null, currentBB));
	}

	@Override
	public void visit(FuncDefNode node) {
		FuncEntity funcEntity = node.getFuncEntity();

		if(!funcEntity.isMember()) {
			currentFunction = new IRFunction(funcEntity);
			ir.addFunction(currentFunction);
		}
		else {
			currentFunction = ir.getFunction(IRFunction.parseName(funcEntity));
		}

		currentScope = node.getBody().getScope();
		currentBB = currentFunction.getBeginBB();

		isParaDecl = true;
		for(Node e : node.getParameterList()) {
			e.accept(this);
		}
		isParaDecl = false;

		if(currentFunction.isMain()) {
			currentBB.appendInst(new IRFuncCall(ir.getFunction(INIT_STATIC_VAR), null, new ArrayList<>(), currentBB));
		}

		node.getBody().accept(this);

		boolean isVoidFunc = funcEntity.isConstruct() || funcEntity.getReturnType() instanceof VoidType;

		if(!currentBB.isEscaped()) currentBB.setJumpInst(new IRReturn(isVoidFunc ? null : new IntImm(0), currentBB));

		List<IRReturn> irReturnList = new ArrayList<>(currentFunction.getIRReturns());

		if(irReturnList.size() > 1) {
			BasicBlock endBB = new BasicBlock(currentFunction, currentFunction.getName() + FUNC_EXIT);
			VirtualReg retReg = isVoidFunc ? null : new VirtualReg(FUNC_RET_VAL);

			for(IRReturn irReturn : irReturnList) {
				BasicBlock block = irReturn.getParentBB();
				if(!isVoidFunc) {
					block.prependInst(irReturn, new IRMove(retReg, irReturn.getRetValue(), block));
				}
				block.removeJumpInst();
				block.setJumpInst(new IRJump(endBB, block));
//				new IRPrinter(System.err).visit(block);
			}

			endBB.setJumpInst(new IRReturn(retReg, endBB));
			currentFunction.setEndBB(endBB);
		}
		else {
			currentFunction.setEndBB(irReturnList.get(0).getParentBB());
		}
	}

	private void classFuncInit(ClassDeclNode node) {
		for (FuncDefNode e : node.getFuncMember()) {
			FuncEntity funcEntity = e.getFuncEntity();
			VarEntity thisEntity = funcEntity.getThisEntity();

			ir.addFunction(new IRFunction(funcEntity));
			thisEntity.setIRRegister(new VirtualReg(THIS_NAME));
		}
	}
	@Override
	public void visit(ClassDeclNode node) {
		currentScope = node.getScope();

		classFuncInit(node);

		for(Node e : node.getFuncMember()) {
			e.accept(this);
		}

		currentScope = currentScope.getParent();
	}

	@Override
	public void visit(VarDeclListNode node) {
		super.visit(node);
	}

	@Override
	public void visit(VarDeclNode node) {
		VarEntity varEntity = node.getVarEntity();

		if(currentScope == globalScope) {
			StaticVar staticVar = new StaticVar(node.getName(), node.getType().getType().getVarSize());
			ir.getStaticDataList().add(staticVar);
			varEntity.setIRRegister(staticVar);

			if(node.getInitVal() != null) {
				globalInits.add(new GlobalInit(staticVar, node.getInitVal()));
			}
		}
		else {
			VirtualReg varReg = new VirtualReg(node.getName());
			varEntity.setIRRegister(varReg);

			if(node.getInitVal() == null) {
				if(isParaDecl) {
					currentFunction.getParaRegs().add(varReg);
				}
				else {
					currentBB.appendInst(new IRMove(varReg, new IntImm(0), currentBB));
				}
			}
			else {
				if(node.getInitVal().isBoolExpr()) {
					node.getInitVal().initFlowBB(currentFunction);
				}
				node.getInitVal().accept(this);
				IRAssign(varReg, 0, node.getInitVal(), false);
			}
		}
	}

	@Override
	public void visit(BlockStmtNode node) {
		currentScope = node.getScope();
		for(Node e : node.getCompound()) {
			safeAccept(e);
			if(currentBB.isEscaped()) break;
		}
		currentScope = currentScope.getParent();
	}

	@Override
	public void visit(CondStmtNode node) {
		BasicBlock thenBB = new BasicBlock(currentFunction, IF_THEN);
		BasicBlock afterBB = new BasicBlock(currentFunction, IF_AFTER);
		BasicBlock elseBB = node.getElseStmt() != null ? new BasicBlock(currentFunction, IF_ELSE) : null;

		node.getCond().setTrueBB(thenBB);
		node.getCond().setFalseBB(elseBB != null ? elseBB : afterBB);
		node.getCond().accept(this);
		if(node.getCond() instanceof BoolConstNode) {
			currentBB.setJumpInst(new IRBranch(node.getCond(), currentBB));
		}

		currentBB = thenBB;
		safeAccept(node.getThenStmt());
		if(!currentBB.isEscaped()) currentBB.setJumpInst(new IRJump(afterBB, currentBB));

		if(node.getElseStmt() != null) {
			currentBB = elseBB;
			node.getElseStmt().accept(this);
			if(!currentBB.isEscaped()) currentBB.setJumpInst(new IRJump(afterBB, currentBB));
		}

		currentBB = afterBB;
	}

	@Override
	public void visit(WhileStmtNode node) {
		BasicBlock condBB = new BasicBlock(currentFunction, WHILE_COND);
		BasicBlock bodyBB = new BasicBlock(currentFunction, WHILE_BODY);
		BasicBlock afterBB = new BasicBlock(currentFunction, WHILE_AFTER);

		BasicBlock outerLoopStepBB = currentLoopStepBB;
		BasicBlock outerLoopAfterBB = currentLoopAfterBB;

		currentLoopStepBB = condBB;
		currentLoopAfterBB = afterBB;

		currentBB.setJumpInst(new IRJump(condBB, currentBB));

		currentBB = condBB;
		node.getCond().setTrueBB(condBB);
		node.getCond().setFalseBB(afterBB);
		node.getCond().accept(this);
		if(node.getCond() instanceof BoolConstNode) {
			currentBB.setJumpInst(new IRBranch(node.getCond(), currentBB));
		}

		currentBB = bodyBB;
		safeAccept(node.getStmt());
		if(!currentBB.isEscaped()) currentBB.setJumpInst(new IRJump(condBB, currentBB));

		currentLoopStepBB = outerLoopStepBB;
		currentLoopAfterBB = outerLoopAfterBB;
		currentBB = afterBB;
	}

	@Override
	public void visit(ForStmtNode node) {
		BasicBlock condBB = new BasicBlock(currentFunction, FOR_COND);
		BasicBlock bodyBB = new BasicBlock(currentFunction, FOR_BODY);
		BasicBlock stepBB = new BasicBlock(currentFunction, FOR_STEP);
		BasicBlock afterBB = new BasicBlock(currentFunction, FOR_AFTER);

		BasicBlock outerLoopStepBB = currentLoopStepBB;
		BasicBlock outerLoopAfterBB = currentLoopAfterBB;

		currentLoopStepBB = stepBB;
		currentLoopAfterBB = afterBB;

		if(node.getInit() != null) {
			node.getInit().accept(this);
		}
		currentBB.setJumpInst(new IRJump(condBB, currentBB));

		currentBB = condBB;
		if(node.getCond() != null) {
			node.getCond().setTrueBB(bodyBB);
			node.getCond().setFalseBB(afterBB);
			node.getCond().accept(this);
			if(node.getCond() instanceof BoolConstNode) {
				currentBB.setJumpInst(new IRBranch(node.getCond(), currentBB));
			}
		}
		else {
			currentBB.setJumpInst(new IRJump(bodyBB, currentBB));
		}

		currentBB = bodyBB;
		if(node.getStmt() != null) {
			node.getStmt().accept(this);
			if(!currentBB.isEscaped()) currentBB.setJumpInst(new IRJump(stepBB, currentBB));
		}
		else {
			currentBB.setJumpInst(new IRJump(stepBB, currentBB));
		}

		currentBB = stepBB;
		if(node.getStep() != null) {
			node.getStep().accept(this);
			currentBB.setJumpInst(new IRJump(condBB, currentBB));
		}
		else {
			currentBB.setJumpInst(new IRJump(condBB, currentBB));
		}

		currentLoopStepBB = outerLoopStepBB;
		currentLoopAfterBB = outerLoopAfterBB;
		currentBB = afterBB;
	}

	@Override
	public void visit(ContinueStmtNode node) {
		currentBB.setJumpInst(new IRJump(currentLoopStepBB, currentBB));
	}

	@Override
	public void visit(BreakStmtNode node) {
		currentBB.setJumpInst(new IRJump(currentLoopAfterBB, currentBB));
	}

	@Override
	public void visit(ReturnStmtNode node) {
		Type returnType = currentFunction.getFuncEntity().getReturnType();
		boolean isVoidFunc = returnType == null || returnType instanceof VoidType;

		if(isVoidFunc) {
			currentBB.setJumpInst(new IRReturn(null, currentBB));
		}
		else {
			if(node.getExpr().isBoolExpr()) {
				node.getExpr().initFlowBB(currentFunction);
				node.getExpr().accept(this);
				VirtualReg resReg = new VirtualReg(null);
				IRAssign(resReg, 0, node.getExpr(), false);
				currentBB.setJumpInst(new IRReturn(resReg, currentBB));
			}
			else {
				node.getExpr().accept(this);
				currentBB.setJumpInst(new IRReturn(node.getExpr().getRegValue(), currentBB));
			}
		}
	}

	private  boolean isMemoryAccess(ExprNode node) {
		if(node instanceof SubscriptExprNode) return true;
		if(node instanceof MemberAccessExprNode) return true;
		if(node instanceof IdentifierExprNode) {
			Entity idEntity = ((IdentifierExprNode) node).getEntity();

			return idEntity instanceof VarEntity &&
				((VarEntity) idEntity).isMember() &&
				((VarEntity) idEntity).getIRRegister() == null;
		}
		return false;
	}

	private IRRegister assignToMemory(ExprNode node, IRBinaryOp.Ops op) {
		VirtualReg resReg = new VirtualReg(null);

		wantAddr = true;
		node.accept(this);

		currentBB.appendInst(new IRBinaryOp(op, resReg, node.getRegValue(), new IntImm(1), currentBB));
		currentBB.appendInst(new IRStore(node.getAddrValue(), node.getAddrOffset(), resReg, currentBB));

		return resReg;
	}

	private IRRegister assignToReg(ExprNode exprNode, IRBinaryOp.Ops op) {
		currentBB.appendInst(new IRBinaryOp(op, (IRRegister) exprNode.getRegValue(), exprNode.getRegValue(),
											new IntImm(1), currentBB));
		return (IRRegister) exprNode.getRegValue();
	}

	@Override
	public void visit(SuffixExprNode node) {
		ExprNode exprNode = node.getExpr();
		boolean toMemory = isMemoryAccess(exprNode);
		boolean outerWantAddr = wantAddr;
		IRBinaryOp.Ops op = node.getOp() == SuffixExprNode.Ops.INC ? IRBinaryOp.Ops.ADD : IRBinaryOp.Ops.SUB;

		wantAddr = false;
		exprNode.accept(this);

		VirtualReg tmpReg = new VirtualReg(null);
		currentBB.appendInst(new IRMove(tmpReg, exprNode.getRegValue(), currentBB));
		node.setRegValue(tmpReg);

		if(toMemory) {
			assignToMemory(exprNode, op);
		}
		else {
			assignToReg(exprNode, op);
		}
		wantAddr = outerWantAddr;
	}

	@Override
	public void visit(PrefixExprNode node) {
		switch (node.getOp()) {
			case INC:
			case DEC:
				ExprNode exprNode = node.getExpr();
				boolean toMemory = isMemoryAccess(exprNode);
				boolean outerWantAddr = wantAddr;
				IRBinaryOp.Ops op = node.getOp() == PrefixExprNode.Ops.INC ? IRBinaryOp.Ops.ADD : IRBinaryOp.Ops.SUB;

				wantAddr = false;
				exprNode.accept(this);

				if(toMemory) {
					node.setRegValue(assignToMemory(exprNode, op));
				}
				else {
					node.setRegValue(assignToReg(exprNode, op));
				}

				wantAddr = outerWantAddr;
				break;

			case POS:
				node.setRegValue(node.getExpr().getRegValue());
				break;

			case NEG:
				VirtualReg negReg = new VirtualReg(null);
				node.setRegValue(negReg);
				node.getExpr().accept(this);
				currentBB.appendInst(new IRUnaryOp(IRUnaryOp.Ops.NEG, negReg, node.getExpr().getRegValue(), currentBB));
				break;

			case BIT_NOT:
				VirtualReg notReg = new VirtualReg(null);
				node.setRegValue(notReg);
				node.getExpr().accept(this);
				currentBB.appendInst(new IRUnaryOp(IRUnaryOp.Ops.BIT_NOT, notReg, node.getExpr().getRegValue(), currentBB));
				break;

			case LOG_NOT:
				node.getExpr().setTrueBB(node.getFalseBB());
				node.getExpr().setFalseBB(node.getTrueBB());
				node.getExpr().accept(this);
				break;

			default:
				assert false;
		}
	}

	@Override
	public void visit(BinaryExprNode node) {
		switch (node.getOp()) {
			case LOG_AND:
			case LOG_OR:
				if(node.hasFlowBB()) {
					if (node.getOp() == BinaryExprNode.Ops.LOG_AND) {
						node.getLhs().setTrueBB(new BasicBlock(currentFunction, LOGIC_INTERRUPT));
						node.getLhs().setFalseBB(node.getFalseBB());
						node.getLhs().accept(this);
						currentBB = node.getLhs().getTrueBB();
					} else {
						node.getLhs().setTrueBB(node.getTrueBB());
						node.getLhs().setFalseBB(new BasicBlock(currentFunction, LOGIC_INTERRUPT));
						node.getLhs().accept(this);
						currentBB = node.getLhs().getFalseBB();
					}
				}
				node.getRhs().setTrueBB(node.getTrueBB());
				node.getRhs().setFalseBB(node.getFalseBB());
				node.getRhs().accept(this);
				break;

			case BIT_XOR: case BIT_AND: case BIT_OR:
			case SHR: case SHL: case MUL: case MOD: case DIV:
			case ADD: case SUB:
				VirtualReg resReg = new VirtualReg(null);

				node.getLhs().accept(this);
				node.getRhs().accept(this);
				node.setRegValue(resReg);
				currentBB.appendInst(new IRBinaryOp(IRBinaryOp.trans(node.getOp()), resReg,
					node.getLhs().getRegValue(), node.getRhs().getRegValue(), currentBB));
				break;

			case LEQ: case GEQ: case LT: case GT:
			case NEQ: case EQ:
				VirtualReg relReg = new VirtualReg(null);

				node.getLhs().accept(this);
				node.getRhs().accept(this);
				currentBB.appendInst(new IRComp(IRComp.trans(node.getOp()), relReg,
					node.getLhs().getRegValue(), node.getRhs().getRegValue(), currentBB));
				if(node.hasFlowBB()) {
					currentBB.setJumpInst(new IRBranch(relReg, node.getTrueBB(), node.getFalseBB(), currentBB));
				}
				break;

			default:
				assert false;
		}
	}

	@Override
	public void visit(SubscriptExprNode node) {
		boolean outerWantAddr = wantAddr;
		wantAddr = false;
		node.getArray().accept(this);
		node.getPostfix().accept(this);
		wantAddr = outerWantAddr;

		VirtualReg reg = new VirtualReg(null);
		IntImm eSize = new IntImm(node.getType().getVarSize());

		currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.MUL, reg, eSize, node.getPostfix().getRegValue(), currentBB));
		currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, reg, reg, node.getArray().getRegValue(), currentBB));

		if(wantAddr) {
			node.setAddrValue(reg);
			node.setAddrOffset(SIZE_PERCH);
		}
		else {
			currentBB.appendInst(new IRLoad(reg, reg, SIZE_PERCH, currentBB));
			node.setRegValue(reg);
			if(node.hasFlowBB()) {
				currentBB.setJumpInst(new IRBranch(node, currentBB));
			}
		}
	}

	@Override
	public void visit(FuncCallExprNode node) {
		FuncEntity funcEntity = node.getFuncEntity();
		List<RegValue> args = new ArrayList<>();

		if(funcEntity.isMember()) {
			if(node.getFunc() instanceof MemberAccessExprNode) {
				ExprNode thisReg = ((MemberAccessExprNode) node.getFunc()).getExpr();
				thisReg.accept(this);
				args.add(thisReg.getRegValue());
			}
			else {
				VarEntity thisEntity = funcEntity.getThisEntity();
 				args.add(thisEntity.getIRRegister());
			}
		}

		for(ExprNode arg : node.getArgs()) {
			arg.accept(this);
			args.add(arg.getRegValue());
		}

		IRFunction irFunction = ir.getFunction(IRFunction.parseName(funcEntity));
		VirtualReg resReg = new VirtualReg(null);

		currentBB.appendInst(new IRFuncCall(irFunction, resReg, args, currentBB));
		node.setRegValue(resReg);

		if(node.hasFlowBB()) {
			currentBB.setJumpInst(new IRBranch(node, currentBB));
		}
	}

	@Override
	public void visit(MemberAccessExprNode node) {
		boolean outerWantAddr = wantAddr;
		wantAddr = false;
		node.getExpr().accept(this);
		wantAddr = outerWantAddr;

		RegValue classAddr = node.getExpr().getRegValue();
		Entity entity = node.getEntity();
//		System.err.println(classAddr == null);
		if(entity instanceof VarEntity) {
//			System.err.println(((VarEntity) entity).getAddrOffset());
			if(wantAddr) {
				node.setAddrValue(classAddr);
				node.setAddrOffset(((VarEntity) entity).getAddrOffset());
			}
			else {
				VirtualReg reg = new VirtualReg(null);
				node.setRegValue(reg);
				currentBB.appendInst(new IRLoad(reg, classAddr, ((VarEntity) entity).getAddrOffset(), currentBB));
				if(node.hasFlowBB()) {
					currentBB.setJumpInst(new IRBranch(node, currentBB));
				}
			}
		}
	}

	@Override
	public void visit(IdentifierExprNode node) {
		assert node.getEntity() instanceof VarEntity;
		VarEntity varEntity = (VarEntity) node.getEntity();

		if(varEntity.getIRRegister() == null) {
			IRRegister thisReg = currentFunction.getFuncEntity().getThisEntity().getIRRegister();
/*
			System.err.println(varEntity.getAddrOffset());
			System.err.println("OK");
			System.err.println(thisReg == null);
*/
			if(wantAddr) {
				node.setAddrValue(thisReg);
				node.setAddrOffset(varEntity.getAddrOffset());
			}
			else {
				VirtualReg resReg = new VirtualReg(null);
				node.setRegValue(resReg);
				currentBB.appendInst(new IRLoad(resReg, thisReg, varEntity.getAddrOffset(), currentBB));
				if(node.hasFlowBB()) currentBB.setJumpInst(new IRBranch(node, currentBB));
			}
		}
		else {
			node.setRegValue(varEntity.getIRRegister());
			if (node.hasFlowBB()) currentBB.setJumpInst(new IRBranch(node, currentBB));
		}
	}

	private void IRAssign(RegValue dest, int offset, ExprNode src, boolean toMemory) {
		if(src.hasFlowBB()) {
			BasicBlock finBB = new BasicBlock(currentFunction, null);
			if(toMemory) {
				src.getTrueBB().appendInst(new IRStore(dest, offset, new IntImm(1), src.getTrueBB()));
				src.getFalseBB().appendInst(new IRStore(dest, offset, new IntImm(0), src.getFalseBB()));
			}
			else {
				src.getTrueBB().appendInst(new IRMove((IRRegister) dest, new IntImm(1), src.getTrueBB()));
				src.getFalseBB().appendInst(new IRMove((IRRegister) dest, new IntImm(0), src.getFalseBB()));
			}
			if(!src.getTrueBB().isEscaped()) src.getTrueBB().setJumpInst(new IRJump(finBB, src.getTrueBB()));
			if(!src.getFalseBB().isEscaped()) src.getFalseBB().setJumpInst(new IRJump(finBB, src.getFalseBB()));
			currentBB = finBB;
		}
		else {
			if(toMemory) {
				currentBB.appendInst(new IRStore(dest, offset, src.getRegValue(), currentBB));
			}
			else {
				currentBB.appendInst(new IRMove((IRRegister) dest, src.getRegValue(), currentBB));
			}
		}
	}

	@Override
	public void visit(AssignExprNode node) {
		boolean toMemory = isMemoryAccess(node.getLhs());
		boolean outerWantAddr = wantAddr;

		wantAddr = toMemory;
		node.getLhs().accept(this);
		wantAddr = outerWantAddr;

		if(node.isBoolExpr()) {
			node.initFlowBB(currentFunction);
		}
		node.getRhs().accept(this);

		if(toMemory) {
			IRAssign(node.getLhs().getAddrValue(), node.getLhs().getAddrOffset(), node.getRhs(), true);
		}
		else {
			IRAssign(node.getLhs().getRegValue(), 0, node.getRhs(), false);
		}
	}

	private void IRNewArray(VirtualReg dest, ArrayType nType, List<RegValue> dimRegs, List<IRRegister> sizeRegs, int post) {
		currentBB.appendInst(new IRHeapAlloc(dest, sizeRegs.get(post), currentBB));
		currentBB.appendInst(new IRStore(dest, 0, dimRegs.get(post), currentBB));

		if(post + 1 < dimRegs.size()) {
			VirtualReg currentPost = new VirtualReg("currentPost" + "_" + post);
			VirtualReg endPost = new VirtualReg("endPost" + "_" + post);
			currentBB.appendInst(new IRMove(currentPost, dest, currentBB));
			currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, endPost, currentPost, sizeRegs.get(post), currentBB));
			if(nType.getBaseType().getVarSize() != SIZE_PERCH) {
				currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, endPost, endPost,
													new IntImm(nType.getBaseType().getVarSize() - SIZE_PERCH), currentBB));
			}
			BasicBlock loopBB = new BasicBlock(currentFunction, "loop_compound");
			BasicBlock afterBB = new BasicBlock(currentFunction, "loop_after");

			currentBB.setJumpInst(new IRJump(loopBB, currentBB));

			currentBB = loopBB;
			VirtualReg tmpReg = new VirtualReg(null);
			VirtualReg cmpReg = new VirtualReg(null);

			IRNewArray(tmpReg, (ArrayType) nType.getBaseType(), dimRegs, sizeRegs, post + 1);
			currentBB.appendInst(new IRStore(currentPost, SIZE_PERCH, tmpReg, currentBB));
			currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, currentPost, currentPost,
													new IntImm(nType.getBaseType().getVarSize()), currentBB));
			currentBB.appendInst(new IRComp(IRComp.Ops.NEQ, cmpReg, currentPost, endPost, currentBB));
			currentBB.setJumpInst(new IRBranch(cmpReg, loopBB, afterBB, currentBB));

			currentBB = afterBB;
		}
	}

	@Override
	public void visit(NewExprNode node) {
		VirtualReg reg = new VirtualReg(null);
		Type newType = node.getNewType().getType();

		if(newType instanceof ClassType) {
			currentBB.appendInst(new IRHeapAlloc(reg, new IntImm(node.getClassSize()), currentBB));
			String className = ((ClassType) newType).getName();
			IRFunction irFunc = ir.getFunction(className + '.' + className);
			if(irFunc != null) {
				List<RegValue> args = new ArrayList<>();
				args.add(reg);
				currentBB.appendInst(new IRFuncCall(irFunc, null, args, currentBB));
			}
		}
		else {
			assert newType instanceof ArrayType;

			List<RegValue> dimRegs = new ArrayList<>();
			List<IRRegister> sizeRegs = new ArrayList<>();
			ArrayType nType = (ArrayType) newType;

			for(int post = 0; post < node.getDims().size(); post++) {
				ExprNode e = node.getDims().get(post);

				boolean outerWantAddr = wantAddr;
				wantAddr = false;
				e.accept(this);
				wantAddr = outerWantAddr;
				dimRegs.add(e.getRegValue());

				VirtualReg sizeReg = new VirtualReg("size" + '_' + post);
				currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.MUL, sizeReg,
						dimRegs.get(post), new IntImm(nType.getBaseType().getVarSize()), currentBB));
				currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, sizeReg,
						sizeReg, new IntImm(SIZE_PERCH), currentBB));
				sizeRegs.add(sizeReg);
			}

			IRNewArray(reg, (ArrayType) newType, dimRegs, sizeRegs,0);
		}
		node.setRegValue(reg);
	}

	@Override
	public void visit(ThisExprNode node) {
		VarEntity varEntity = (VarEntity) node.getEntity();
		node.setRegValue(varEntity.getIRRegister());
	}

	@Override
	public void visit(StringConstNode node) {
		node.setRegValue(new StaticString(node.getValue()));
	}

	@Override
	public void visit(IntConstNode node) {
		node.setRegValue(new IntImm(node.getValue()));
	}

	@Override
	public void visit(NullLiteralNode node) {
		node.setRegValue(new IntImm(0));
	}

	@Override
	public void visit(BoolConstNode node) {
		node.setRegValue(new IntImm(node.getValue() ? 1 : 0));
	}

	@Override
	public void visit(TypeNode node) {
		super.visit(node);
	}

}
