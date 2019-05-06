package mxstar.frontend;

import mxstar.ast.*;
import mxstar.ir.*;
import mxstar.symbol.scope.*;
import mxstar.symbol.type.*;

import java.util.ArrayList;
import java.util.List;

import static mxstar.utility.GlobalSymbols.*;

public class IRBuilder extends ASTBaseVisitor {
	private Scope globalScope;

	private IRRoot ir = new IRRoot();
	private Scope currentScope = null;
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
		ir.addBuiltInFunctions(analyser.getBuiltInFuncEntityList());
	}

	private void IRAssign(RegValue dest, int offset, ExprNode src, boolean toMemory) {
		if (src.isBoolExpr()) {
			BasicBlock finBB = new BasicBlock(currentFunction, null);
			if (toMemory) {
				src.getTrueBB().appendInst(new IRStore(dest, offset, new IntImm(1), src.getTrueBB()));
				src.getFalseBB().appendInst(new IRStore(dest, offset, new IntImm(0), src.getFalseBB()));
			} else {
				src.getTrueBB().appendInst(new IRMove((IRRegister) dest, new IntImm(1), src.getTrueBB()));
				src.getFalseBB().appendInst(new IRMove((IRRegister) dest, new IntImm(0), src.getFalseBB()));
			}
			if (!src.getTrueBB().isEscaped()) src.getTrueBB().setJumpInst(new IRJump(finBB, src.getTrueBB()));
			if (!src.getFalseBB().isEscaped()) src.getFalseBB().setJumpInst(new IRJump(finBB, src.getFalseBB()));
			currentBB = finBB;
		} else {
			if (toMemory) {
				currentBB.appendInst(new IRStore(dest, offset, src.getRegValue(), currentBB));
			} else {
				currentBB.appendInst(new IRMove((IRRegister) dest, src.getRegValue(), currentBB));
			}
		}
	}

	@Override
	public void visit(ASTRootNode node) {
		currentScope = globalScope;
		ir.addFunction(new IRFunction(new FuncEntity(VoidType.getInstance(), INIT_STATIC_VAR, new ArrayList<>(), null)));

		List<FuncDefNode> funcDefNodeList = new ArrayList<>();

		for (Node declNode : node.getDecls()) {
			if (declNode instanceof FuncDefNode) {
				FuncDefNode funcDefNode = (FuncDefNode) declNode;
				ir.addFunction(new IRFunction(funcDefNode.getFuncEntity()));
				funcDefNodeList.add(funcDefNode);
			}
		}

		for(Node declNode : node.getDecls()) {
			if (declNode instanceof ClassDeclNode) {
				ClassDeclNode classDeclNode = (ClassDeclNode) declNode;
				for(FuncDefNode funcDefNode : classDeclNode.getFuncMember()) {
					ir.addFunction(new IRFunction(funcDefNode.getFuncEntity()));
					funcDefNodeList.add(funcDefNode);
				}
			}
		}

		for(Node declNode : node.getDecls()) {
			if(declNode instanceof VarDeclListNode) {
				visit((VarDeclListNode) declNode);
			}
		}

		currentFunction = ir.getFunction(INIT_STATIC_VAR);
		currentBB = currentFunction.getBeginBB();
		for (GlobalInit globalInit : globalInits) {
			if (globalInit.getSrc().isBoolExpr()) {
				globalInit.getSrc().initFlowBB(currentFunction);
			}
			globalInit.getSrc().accept(this);
			IRAssign(globalInit.getDest(), 0, globalInit.getSrc(), false);
		}
		currentBB.setJumpInst(new IRReturn(null, currentBB));

		for(FuncDefNode funcDefNode : funcDefNodeList) {
			visit(funcDefNode);
		}

		for (IRFunction irFunction : ir.getFunctionList()) {
			irFunction.updateCalleeSet();
		}
		ir.updateCalleeSet();
	}

	@Override
	public void visit(FuncDefNode node) {
		FuncEntity funcEntity = node.getFuncEntity();

		currentFunction = ir.getFunction(IRFunction.parseName(funcEntity));
		currentScope = node.getBody().getScope();
		currentBB = currentFunction.getBeginBB();

		if (funcEntity.isMember()) {
			VarEntity entity = funcEntity.getThisEntity();
			VirtualReg thisReg = new VirtualReg(THIS_NAME);
			entity.setIRRegister(thisReg);
			currentFunction.getParaRegs().add(thisReg);
		}

		isParaDecl = true;
		for (Node para : node.getParameterList()) {
			para.accept(this);
		}
		isParaDecl = false;

		if (currentFunction.isMain()) {
			currentBB.appendInst(new IRFunctionCall(ir.getFunction(INIT_STATIC_VAR), null, new ArrayList<>(), currentBB));
		}

		node.getBody().accept(this);

		boolean isVoidFunc = funcEntity.isConstruct() || funcEntity.getReturnType() instanceof VoidType;

		if (!currentBB.isEscaped()) currentBB.setJumpInst(new IRReturn(isVoidFunc ? null : new IntImm(0), currentBB));

		List<IRReturn> irReturnList = new ArrayList<>(currentFunction.getIRReturns());

		if (irReturnList.size() > 1) {
			BasicBlock endBB = new BasicBlock(currentFunction, currentFunction.getName() + FUNC_EXIT);
			VirtualReg retReg = isVoidFunc ? null : new VirtualReg(FUNC_RET_VAL);

			for (IRReturn irReturn : irReturnList) {
				BasicBlock block = irReturn.getParentBB();
				if (!isVoidFunc) {
					block.prependInst(irReturn, new IRMove(retReg, irReturn.getRetValue(), block));
				}
				block.removeJumpInst();
				block.setJumpInst(new IRJump(endBB, block));
			}

			endBB.setJumpInst(new IRReturn(retReg, endBB));
		}
	}


	@Override
	public void visit(VarDeclNode node) {
		VarEntity varEntity = node.getVarEntity();

		if (currentScope == globalScope) {
			StaticVar staticVar = new StaticVar(node.getName(), node.getType().getType().getVarSize());
			ir.getStaticDataList().add(staticVar);
			varEntity.setIRRegister(staticVar);

			if (node.getInitVal() != null) {
				globalInits.add(new GlobalInit(staticVar, node.getInitVal()));
			}
		} else {
			VirtualReg varReg = new VirtualReg(node.getName());
			varEntity.setIRRegister(varReg);

			if (node.getInitVal() == null) {
				if (isParaDecl) {
					currentFunction.getParaRegs().add(varReg);
				} else {
					currentBB.appendInst(new IRMove(varReg, new IntImm(0), currentBB));
				}
			} else {
				if (node.getInitVal().isBoolExpr()) {
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
		for (Node compound : node.getCompound()) {
			safeAccept(compound);
			if (currentBB.isEscaped()) break;
		}
		currentScope = currentScope.getParent();
	}

	@Override
	public void visit(CondStmtNode node) {
		BasicBlock thenBB = new BasicBlock(currentFunction, IF_THEN);
		BasicBlock afterBB = new BasicBlock(currentFunction, IF_AFTER);
		BasicBlock elseBB = new BasicBlock(currentFunction, IF_ELSE);

		node.getCond().setTrueBB(thenBB);
		node.getCond().setFalseBB(elseBB);
		node.getCond().accept(this);
		if (node.getCond() instanceof BoolConstNode) {
			currentBB.setJumpInst(new IRBranch(node.getCond(), currentBB));
		}

		currentBB = thenBB;
		if (node.getThenStmt() != null) {
			node.getThenStmt().accept(this);
		}
		if (!currentBB.isEscaped()) currentBB.setJumpInst(new IRJump(afterBB, currentBB));

		currentBB = elseBB;
		if (node.getElseStmt() != null) {
			node.getElseStmt().accept(this);
		}
		if (!currentBB.isEscaped()) currentBB.setJumpInst(new IRJump(afterBB, currentBB));

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
		node.getCond().setTrueBB(bodyBB);
		node.getCond().setFalseBB(afterBB);
		node.getCond().accept(this);
		if (node.getCond() instanceof BoolConstNode) {
			currentBB.setJumpInst(new IRBranch(node.getCond(), currentBB));
		}

		currentBB = bodyBB;
		if(node.getStmt() != null){
			node.getStmt().accept(this);
		}
		if (!currentBB.isEscaped()) currentBB.setJumpInst(new IRJump(condBB, currentBB));

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

		if (node.getInit() != null) {
			node.getInit().accept(this);
		}
		currentBB.setJumpInst(new IRJump(condBB, currentBB));

		currentBB = condBB;
		if (node.getCond() != null) {
			node.getCond().setTrueBB(bodyBB);
			node.getCond().setFalseBB(afterBB);
			node.getCond().accept(this);
			if (node.getCond() instanceof BoolConstNode) {
				currentBB.setJumpInst(new IRBranch(node.getCond(), currentBB));
			}
		} else {
			currentBB.setJumpInst(new IRJump(bodyBB, currentBB));
		}

		currentBB = bodyBB;
		if (node.getStmt() != null) {
			node.getStmt().accept(this);
			if (!currentBB.isEscaped()) currentBB.setJumpInst(new IRJump(stepBB, currentBB));
		} else {
			currentBB.setJumpInst(new IRJump(stepBB, currentBB));
		}

		currentBB = stepBB;
		if (node.getStep() != null) {
			node.getStep().accept(this);
			currentBB.setJumpInst(new IRJump(condBB, currentBB));
		} else {
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

		if (isVoidFunc) {
			currentBB.setJumpInst(new IRReturn(null, currentBB));
		} else {
			if (node.getExpr().isBoolExpr()) {
				node.getExpr().initFlowBB(currentFunction);
				node.getExpr().accept(this);
				VirtualReg resReg = new VirtualReg(null);
				IRAssign(resReg, 0, node.getExpr(), false);
				currentBB.setJumpInst(new IRReturn(resReg, currentBB));
			} else {
				node.getExpr().accept(this);
				currentBB.setJumpInst(new IRReturn(node.getExpr().getRegValue(), currentBB));
			}
		}
	}

	private boolean isMemoryAccess(ExprNode node) {
		if (node instanceof SubscriptExprNode) return true;
		else if (node instanceof MemberAccessExprNode) return true;
		else if (node instanceof IdentifierExprNode) {
			Entity idEntity = ((IdentifierExprNode) node).getEntity();

			if(idEntity instanceof  VarEntity && ((VarEntity) idEntity).isMember()) {
				assert (((VarEntity) idEntity).getIRRegister() == null);
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	private void RealAssign(ExprNode exprNode, VirtualReg reg, boolean toMemory) {
		if(toMemory) {
			wantAddr = true;
			exprNode.accept(this);

			currentBB.appendInst(new IRStore(exprNode.getAddrValue(), exprNode.getAddrOffset(), reg, currentBB));
		}
		else {
			RegValue dest = exprNode.getRegValue();

			assert dest instanceof IRRegister;
			currentBB.appendInst(new IRMove((IRRegister) dest, reg, currentBB));
		}
	}

	@Override
	public void visit(SuffixExprNode node) {
		ExprNode exprNode = node.getExpr();
		boolean toMemory = isMemoryAccess(exprNode);
		boolean outerWantAddr = wantAddr;
		IRBinaryOp.Ops op = node.getOp() == SuffixExprNode.Ops.INC ? IRBinaryOp.Ops.ADD : IRBinaryOp.Ops.SUB;

		wantAddr = false;
		exprNode.accept(this);

		VirtualReg returnReg = new VirtualReg(null);
		currentBB.appendInst(new IRMove(returnReg, exprNode.getRegValue(), currentBB));
		node.setRegValue(returnReg);

		VirtualReg resReg = new VirtualReg(null);
		currentBB.appendInst(new IRBinaryOp(op, resReg, returnReg, new IntImm(1), currentBB));
		RealAssign(exprNode, resReg, toMemory);
		wantAddr = outerWantAddr;
	}

	@Override
	public void visit(PrefixExprNode node) {
		ExprNode exprNode = node.getExpr();
		VirtualReg resReg = new VirtualReg(null);

		switch (node.getOp()) {
			case INC:
			case DEC:
				boolean toMemory = isMemoryAccess(exprNode);
				boolean outerWantAddr = wantAddr;
				IRBinaryOp.Ops op = node.getOp() == PrefixExprNode.Ops.INC ? IRBinaryOp.Ops.ADD : IRBinaryOp.Ops.SUB;

				wantAddr = false;
				exprNode.accept(this);

				currentBB.appendInst(new IRBinaryOp(op, resReg, exprNode.getRegValue(), new IntImm(1), currentBB));
				RealAssign(exprNode, resReg, toMemory);
				exprNode.setRegValue(resReg);

				wantAddr = outerWantAddr;
				break;

			case POS:
				node.setRegValue(exprNode.getRegValue());
				break;

			case NEG:
				node.setRegValue(resReg);
				exprNode.accept(this);
				currentBB.appendInst(new IRUnaryOp(IRUnaryOp.Ops.NEG, resReg, exprNode.getRegValue(), currentBB));
				break;

			case BIT_NOT:
				node.setRegValue(resReg);
				exprNode.accept(this);
				currentBB.appendInst(new IRUnaryOp(IRUnaryOp.Ops.BIT_NOT, resReg, exprNode.getRegValue(), currentBB));
				break;

			case LOG_NOT:
				exprNode.setTrueBB(node.getFalseBB());
				exprNode.setFalseBB(node.getTrueBB());
				exprNode.accept(this);
				break;

			default:
				assert false;
		}
	}

	@Override
	public void visit(BinaryExprNode node) {

		VirtualReg resReg = new VirtualReg(null);

		switch (node.getOp()) {
			case LOG_AND:
			case LOG_OR:

				if (node.hasFlowBB()) {
					BasicBlock interBB = new BasicBlock(currentFunction, LOGIC_INTERRUPT);

					if (node.getOp() == BinaryExprNode.Ops.LOG_AND) {
						node.getLhs().setTrueBB(interBB);
						node.getLhs().setFalseBB(node.getFalseBB());
						node.getLhs().accept(this);
					} else {
						node.getLhs().setTrueBB(node.getTrueBB());
						node.getLhs().setFalseBB(interBB);
						node.getLhs().accept(this);
					}
					currentBB = interBB;
					node.getRhs().setTrueBB(node.getTrueBB());
					node.getRhs().setFalseBB(node.getFalseBB());
					node.getRhs().accept(this);
				}
				break;

			case SHR:
			case SHL:
			case MOD:
			case DIV:
			case BIT_XOR:
			case BIT_AND:
			case BIT_OR:
			case MUL:
			case ADD:
			case SUB:

				if(node.getLhs().getType() instanceof StringType) {
					DoStringBinaryOp(node);
					break;
				}

				node.getLhs().accept(this);
				node.getRhs().accept(this);
				node.setRegValue(resReg);
				currentBB.appendInst(new IRBinaryOp(
						IRBinaryOp.trans(node.getOp()),
						resReg,
						node.getLhs().getRegValue(),
						node.getRhs().getRegValue(),
						currentBB
				));
				break;

			case LEQ:
			case GEQ:
			case LT:
			case GT:
			case NEQ:
			case EQ:

				if(node.getLhs().getType() instanceof StringType) {
					DoStringBinaryOp(node);
					break;
				}

				node.getLhs().accept(this);
				node.getRhs().accept(this);
				node.setRegValue(resReg);
				currentBB.appendInst(new IRComparison(
						IRComparison.trans(node.getOp()),
						resReg,
						node.getLhs().getRegValue(),
						node.getRhs().getRegValue(),
						currentBB
				));

				if (node.hasFlowBB()) {
					currentBB.setJumpInst(new IRBranch(resReg, node.getTrueBB(), node.getFalseBB(), currentBB));
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

		VirtualReg addrReg = new VirtualReg(null);
		VirtualReg sizeReg = new VirtualReg(null);
		IntImm per = new IntImm(node.getType().getVarSize());

		currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.MUL, sizeReg, per, node.getPostfix().getRegValue(), currentBB));
		currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, addrReg, sizeReg, node.getArray().getRegValue(), currentBB));

		if (wantAddr) {
			node.setAddrValue(addrReg);
			node.setAddrOffset(SIZE_PERCH);
		} else {
			VirtualReg valueReg = new VirtualReg(null);

			currentBB.appendInst(new IRLoad(valueReg, addrReg, SIZE_PERCH, currentBB));
			node.setRegValue(valueReg);
			if (node.hasFlowBB()) {
				currentBB.setJumpInst(new IRBranch(node, currentBB));
			}
		}
	}

	@Override
	public void visit(FuncCallExprNode node) {
		FuncEntity funcEntity = node.getFuncEntity();
		List<RegValue> args = new ArrayList<>();

		if (funcEntity.isMember()) {
			if (node.getFunc() instanceof MemberAccessExprNode) {
				ExprNode thisExpr = ((MemberAccessExprNode) node.getFunc()).getExpr();
				thisExpr.accept(this);
				args.add(thisExpr.getRegValue());
			} else {
				VarEntity thisEntity = currentFunction.getFuncEntity().getThisEntity();
				args.add(thisEntity.getIRRegister());
			}
		}

		for (ExprNode arg : node.getArgs()) {
			arg.accept(this);
			args.add(arg.getRegValue());
		}

		IRFunction irFunction = funcEntity.isBuiltIn() ?
								ir.getBuiltInFunction(IRFunction.parseName(funcEntity)) :
								ir.getFunction(IRFunction.parseName(funcEntity));

		VirtualReg resReg = new VirtualReg(null);

		if(irFunction.isTrivial()) {
			assert args.size() == 1;
			currentBB.appendInst(new IRLoad(resReg, args.get(0), 0, currentBB));
		}
		else {
			currentBB.appendInst(new IRFunctionCall(irFunction, resReg, args, currentBB));
		}

		node.setRegValue(resReg);

		if (node.hasFlowBB()) {
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
		VarEntity memberEntity = (VarEntity) node.getEntity();

		if (wantAddr) {
			node.setAddrValue(classAddr);
			node.setAddrOffset(memberEntity.getAddrOffset());
		} else {
			VirtualReg reg = new VirtualReg(null);
			node.setRegValue(reg);
			currentBB.appendInst(new IRLoad(reg, classAddr, memberEntity.getAddrOffset(), currentBB));
			if (node.hasFlowBB()) {
				currentBB.setJumpInst(new IRBranch(node, currentBB));
			}
		}
	}

	@Override
	public void visit(IdentifierExprNode node) {
		VarEntity varEntity = (VarEntity) node.getEntity();

		if (varEntity.getIRRegister() == null) {
			System.err.println( node.location().toString() );
			IRRegister thisReg = currentFunction.getFuncEntity().getThisEntity().getIRRegister();
			System.err.println( node.location().toString() );

			if (wantAddr) {
				node.setAddrValue(thisReg);
				node.setAddrOffset(varEntity.getAddrOffset());
			} else {
				VirtualReg resReg = new VirtualReg(null);
				node.setRegValue(resReg);
				currentBB.appendInst(new IRLoad(resReg, thisReg, varEntity.getAddrOffset(), currentBB));
				if (node.hasFlowBB()) currentBB.setJumpInst(new IRBranch(node, currentBB));
			}
		} else {
			node.setRegValue(varEntity.getIRRegister());
			if (node.hasFlowBB()) currentBB.setJumpInst(new IRBranch(node, currentBB));
		}
	}


	@Override
	public void visit(AssignExprNode node) {
		boolean toMemory = isMemoryAccess(node.getLhs());
		boolean outerWantAddr = wantAddr;

		wantAddr = toMemory;
		node.getLhs().accept(this);
		wantAddr = outerWantAddr;

		if (node.getRhs().isBoolExpr()) {
			node.getRhs().initFlowBB(currentFunction);
		}
		node.getRhs().accept(this);

		if (toMemory) {
			IRAssign(node.getLhs().getAddrValue(), node.getLhs().getAddrOffset(), node.getRhs(), true);
		} else {
			IRAssign(node.getLhs().getRegValue(), 0, node.getRhs(), false);
		}
	}

	private void IRNewArray(VirtualReg dest, ArrayType nType, List<RegValue> dimRegs, List<IRRegister> sizeRegs, int post) {
		currentBB.appendInst(new IRHeapAlloc(dest, sizeRegs.get(post), currentBB));
		currentBB.appendInst(new IRStore(dest, 0, dimRegs.get(post), currentBB));

		if (post + 1 < dimRegs.size()) {
			VirtualReg currentPost = new VirtualReg("currentPost" + "_" + post);
			VirtualReg endPost = new VirtualReg("endPost" + "_" + post);
			currentBB.appendInst(new IRMove(currentPost, dest, currentBB));
			currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, endPost, currentPost, sizeRegs.get(post), currentBB));
			if (nType.getBaseType().getVarSize() != SIZE_PERCH) {
				currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, endPost, endPost,
						new IntImm(nType.getBaseType().getVarSize() - SIZE_PERCH), currentBB));
			}
			BasicBlock loopBB = new BasicBlock(currentFunction, "loop_body");
			BasicBlock afterBB = new BasicBlock(currentFunction, "loop_after");

			currentBB.setJumpInst(new IRJump(loopBB, currentBB));

			currentBB = loopBB;
			VirtualReg tmpReg = new VirtualReg(null);
			VirtualReg cmpReg = new VirtualReg(null);

			IRNewArray(tmpReg, (ArrayType) nType.getBaseType(), dimRegs, sizeRegs, post + 1);
			currentBB.appendInst(new IRStore(currentPost, SIZE_PERCH, tmpReg, currentBB));
			currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, currentPost, currentPost,
					new IntImm(nType.getBaseType().getVarSize()), currentBB));
			currentBB.appendInst(new IRComparison(IRComparison.Ops.NEQ, cmpReg, currentPost, endPost, currentBB));
			currentBB.setJumpInst(new IRBranch(cmpReg, loopBB, afterBB, currentBB));

			currentBB = afterBB;
		}
	}

	@Override
	public void visit(NewExprNode node) {
		VirtualReg reg = new VirtualReg(null);
		Type newType = node.getNewType().getType();

		if (newType instanceof ClassType) {
			currentBB.appendInst(new IRHeapAlloc(reg, new IntImm(node.getClassSize()), currentBB));
			String className = ((ClassType) newType).getName();
			IRFunction irFunc = ir.getFunction(className + '.' + className);
			if (irFunc != null) {
				List<RegValue> args = new ArrayList<>();
				args.add(reg);
				currentBB.appendInst(new IRFunctionCall(irFunc, null, args, currentBB));
			}
		} else {
			assert newType instanceof ArrayType;

			List<RegValue> dimRegs = new ArrayList<>();
			List<IRRegister> sizeRegs = new ArrayList<>();
			ArrayType nType = (ArrayType) newType;

			for (int post = 0; post < node.getDims().size(); post++) {
				ExprNode dim = node.getDims().get(post);

				boolean outerWantAddr = wantAddr;
				wantAddr = false;
				dim.accept(this);
				wantAddr = outerWantAddr;
				dimRegs.add(dim.getRegValue());

				VirtualReg sizeReg = new VirtualReg("size" + '_' + post);
				currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.MUL, sizeReg,
						dimRegs.get(post), new IntImm(nType.getBaseType().getVarSize()), currentBB));
				currentBB.appendInst(new IRBinaryOp(IRBinaryOp.Ops.ADD, sizeReg,
						sizeReg, new IntImm(SIZE_PERCH), currentBB));
				sizeRegs.add(sizeReg);
			}

			IRNewArray(reg, (ArrayType) newType, dimRegs, sizeRegs, 0);
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
		StaticStr staticStr = ir.staticStrMap.get(node.getValue());
		if (staticStr == null) {
			staticStr = new StaticStr(node.getValue());
			ir.staticStrMap.put(node.getValue(), staticStr);
		}
		node.setRegValue(staticStr);
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


	private void DoStringBinaryOp(BinaryExprNode node) {
		IRFunction calleeFunc = null;
		boolean toReverse = false;

		switch (node.getOp()) {
			case ADD:
				calleeFunc = ir.getBuiltInFunction("__string_concat");
				break;
			case EQ:
				calleeFunc = ir.getBuiltInFunction("__string_eq");
				break;
			case NEQ:
				calleeFunc = ir.getBuiltInFunction("__string_neq");
				break;
			case LT:
				calleeFunc = ir.getBuiltInFunction("__string_lt");
				break;
			case LEQ:
				calleeFunc = ir.getBuiltInFunction("__string_leq");
				break;
			case GT:
				calleeFunc = ir.getBuiltInFunction("__string_lt");
				toReverse = true;
				break;
			case GEQ:
				calleeFunc = ir.getBuiltInFunction("__string_leq");
				toReverse = true;
				break;
			default:
				throw new Error("can't find string op");
		}

		node.getLhs().accept(this);
		node.getRhs().accept(this);

		List<RegValue> args = new ArrayList<>();
		if (!toReverse) {
			args.add(node.getLhs().getRegValue());
			args.add(node.getRhs().getRegValue());
		} else {
			args.add(node.getRhs().getRegValue());
			args.add(node.getRhs().getRegValue());
		}

		VirtualReg resReg = new VirtualReg(null);
		currentBB.appendInst(new IRFunctionCall(calleeFunc, resReg, args, currentBB));

		if(calleeFunc == null) {
			System.err.println(node.getOp().toString());
			throw new Error("string");
		}

		node.setRegValue(resReg);

		if(node.hasFlowBB()) {
			currentBB.setJumpInst(new IRBranch(node, currentBB));
		}
	}
}