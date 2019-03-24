package mxstar.frontend;

import mxstar.ast.*;
import mxstar.symbol.scope.*;
import mxstar.utility.Location;
import mxstar.utility.error.SemanticError;
import mxstar.symbol.type.*;

import java.util.*;

import static mxstar.utility.GlobalSymbols.*;

public class SemanticAnalyser extends ASTBaseVisitor {
	private Scope globalScope = null;
	private Scope currentScope = null;
	private ClassEntity currentClassEntity = null;
	private Type currentReturnType = null;
	private int inLoop = 0;
	private int currentOffset = 0;
	private List<FuncEntity> builtInFuncEntity = new ArrayList<>();

	public SemanticAnalyser() {}

	public Scope getGlobalScope() {
		return globalScope;
	}

	public List<FuncEntity> getBuiltInFuncEntity() {
		return builtInFuncEntity;
	}

	private void insertBuiltInFunction(Type returnType,
	                                   String name,
	                                   List<VarEntity> paraList,
	                                   ClassEntity classEntity,
	                                   Location location) {
		FuncEntity funcEntity = new FuncEntity(returnType, name, paraList, classEntity);
		Scope thisScope = funcEntity.isMember() ? classEntity.getScope() : globalScope;
		thisScope.assertInsert(FUNC_PREFIX + name, funcEntity, location);

		if(funcEntity.isMember()) {
			VarEntity thisEntity = new VarEntity(classEntity.getType(), THIS_NAME);
			funcEntity.setThisEntity(thisEntity);
		}

		builtInFuncEntity.add(funcEntity);
	}

	private void insertBuiltInFunctions(Location location) {
		insertBuiltInFunction(VoidType.getInstance(), "print",
							Collections.singletonList(new VarEntity(StringType.getInstance(), "string")),
							null, location);
		insertBuiltInFunction(VoidType.getInstance(), "println",
							Collections.singletonList(new VarEntity(StringType.getInstance(), "string")),
							null, location);
		insertBuiltInFunction(StringType.getInstance(), "getString",
							new ArrayList<>(), null, location);
		insertBuiltInFunction(IntType.getInstance(), "getInt",
							new ArrayList<>(), null, location);
		insertBuiltInFunction(StringType.getInstance(), "toString",
							Collections.singletonList(new VarEntity(IntType.getInstance(), "value")),
							null, location);

		Type stringType = new ClassType(STR_CLASS_NAME);
		ClassEntity stringEntity = new ClassEntity(stringType, STR_CLASS_NAME);
		globalScope.assertInsert(CLASS_PREFIX + stringEntity.getName(), stringEntity, location);
		stringEntity.setScope(new Scope(globalScope));
		insertBuiltInFunction(IntType.getInstance(), "length",
							Collections.singletonList(new VarEntity(stringType, THIS_NAME)),
							stringEntity, location);
		insertBuiltInFunction(StringType.getInstance(), "substring",
							Arrays.asList(new VarEntity(stringType, THIS_NAME),
										  new VarEntity(IntType.getInstance(), "begin"),
										  new VarEntity(IntType.getInstance(), "end")),
							stringEntity, location);
		insertBuiltInFunction(IntType.getInstance(), "parseInt",
							Collections.singletonList(new VarEntity(stringType, THIS_NAME)),
							stringEntity, location);
		insertBuiltInFunction(IntType.getInstance(), "ord",
							Arrays.asList(new VarEntity(stringType, THIS_NAME),
										  new VarEntity(IntType.getInstance(), "position")),
							stringEntity, location);

		Type arrayType = new ClassType(ARRAY_CLASS_NAME);
		ClassEntity arrayEntity = new ClassEntity(arrayType, ARRAY_CLASS_NAME);
		globalScope.assertInsert(CLASS_PREFIX + arrayEntity.getName(), arrayEntity, location);
		arrayEntity.setScope(new Scope(globalScope));
		insertBuiltInFunction(IntType.getInstance(), "size",
							Collections.singletonList(new VarEntity(arrayType, THIS_NAME)),
							arrayEntity, location);
	}

	@Override
	public void visit(ASTRootNode node) {
		globalScope = currentScope = new Scope();

		insertBuiltInFunctions(node.location());
		for(Node declNode : node.getDecls()) {
			if(declNode instanceof ClassDeclNode) {
				ClassDeclNode classDeclNode = (ClassDeclNode) declNode;

				ClassEntity classEntity = new ClassEntity(classDeclNode);

				classDeclNode.setClassEntity(classEntity);
				globalScope.assertInsert(CLASS_PREFIX + classEntity.getName(), classEntity, classDeclNode.location());
			}
		}

		for(Node declNode : node.getDecls()) {
			if(declNode instanceof FuncDefNode) {
				FuncDefNode funcDefNode = (FuncDefNode) declNode;
				FuncEntity funcEntity = new FuncEntity(funcDefNode, null);

				funcDefNode.setFuncEntity(funcEntity);
				globalScope.assertInsert(FUNC_PREFIX + funcDefNode.getName(), funcEntity, funcDefNode.location());
			}
		}

		for(Node declNode : node.getDecls()) {
			if(declNode instanceof ClassDeclNode) declNode.accept(this);
		}
		for(Node declNode : node.getDecls()) {
			if(declNode instanceof ClassDeclNode) {
				visitFuncMember((ClassDeclNode) declNode);
			}
			else {
				declNode.accept(this);
			}
		}

		FuncEntity mainFunc = (FuncEntity) globalScope.find(FUNC_PREFIX + "main");
		if(mainFunc == null) {
			throw new SemanticError("main function not found", node.location());
		}
		if(!(mainFunc.getReturnType() instanceof IntType)) {
			throw new SemanticError("mainFunc return value IntType Expected", node.location());
		}
		if(!mainFunc.getParameters().isEmpty()) {
			throw new SemanticError("mainFunc Unexpected Parameters", node.location());
		}
		currentScope = null;
	}

	@Override
	public void visit(VarDeclListNode node) {
		super.visit(node);
	}

	@Override
	public void visit(VarDeclNode node) {
		Type declType = node.getType().getType();

		if(declType instanceof ClassType) {
			currentScope.assertContains(CLASS_PREFIX + ((ClassType) declType).getName(), node.location());
		}

		if(declType instanceof VoidType) {
			throw new SemanticError("Unexpected VoidType", node.location());
		}

		if(node.getInitVal() != null) {
			node.getInitVal().accept(this);
			Type initType = node.getInitVal().getType();

			if(initType instanceof NullLiteral) {
				if(!(declType instanceof ArrayType || declType instanceof ClassType))
					throw new SemanticError("Unexpected InitValue", node.location());
			}
			else {
				if(!(initType.equals(declType)))
					throw new SemanticError("Unexpected InitValue", node.location());
			}
		}

		VarEntity varEntity = new VarEntity(node);
		node.setVarEntity(varEntity);

		if(currentClassEntity != null) {
			varEntity.setMember(true);
			varEntity.setAddrOffset(currentOffset);
			currentOffset += varEntity.getType().getVarSize();
		}
		currentScope.assertInsert(VAR_PREFIX + node.getName(), varEntity, node.location());
	}

	@Override
	public void visit(FuncDefNode node) {
		FuncEntity funcEntity = node.getFuncEntity();

		currentReturnType = funcEntity.getReturnType();
		if(currentReturnType instanceof ClassType) {
			currentScope.assertContains(CLASS_PREFIX + ((ClassType) currentReturnType).getName(),
										node.location());
		}

		currentScope = new Scope(currentScope);

		if(funcEntity.isConstruct()) {
			if (currentClassEntity == null || !(node.getName().equals(currentClassEntity.getName()))) {
				throw new SemanticError("ReturnType Expected", node.location());
			}
			if (!node.getParameterList().isEmpty()) {
				throw new SemanticError("Forbidden Construct function", node.location());
			}
		}

		if(funcEntity.isMember()) {
			VarEntity thisEntity = new VarEntity(currentClassEntity.getType(), THIS_NAME);

			currentScope.assertInsert(VAR_PREFIX + THIS_NAME,
									  thisEntity,
									  node.location());

			funcEntity.setThisEntity(thisEntity);
		}

		for(Node varDeclNode : node.getParameterList()) varDeclNode.accept(this);

		node.getBody().setScope(currentScope);
		node.getBody().accept(this);
		currentReturnType = null;
	}

	@Override
	public void visit(ClassDeclNode node) {
		ClassEntity classEntity = node.getClassEntity();

		currentClassEntity = classEntity;
		currentScope = new Scope(currentScope);
		node.setScope(currentScope);
		classEntity.setScope(currentScope);

		currentOffset = 0;
		for(Node varMemberNode : node.getVarMember()) varMemberNode.accept(this);
		classEntity.setWidth(currentOffset);

		for(FuncDefNode funcMemberNode : node.getFuncMember()) {
			FuncEntity funcEntity = new FuncEntity(funcMemberNode, currentClassEntity);
			funcMemberNode.setFuncEntity(funcEntity);
			currentScope.assertInsert(FUNC_PREFIX + funcMemberNode.getName(), funcEntity, funcMemberNode.location());
		}

		currentClassEntity = null;
		currentScope = currentScope.getParent();
	}

	private void visitFuncMember(ClassDeclNode node) {
		currentClassEntity = node.getClassEntity();
		currentScope = node.getScope();

		for(Node funcMemberNode : node.getFuncMember()) {
			funcMemberNode.accept(this);
		}
		currentClassEntity = null;
		currentScope = currentScope.getParent();
	}

	@Override
	public void visit(BlockStmtNode node) {
		if(node.getScope() == null) {
			currentScope = new Scope(currentScope);
			node.setScope(currentScope);
		}
		else {
			assert currentScope == node.getScope();
		}

		for(Node compoundNode : node.getCompound()) safeAccept(compoundNode);

		currentScope = currentScope.getParent();
	}

	@Override
	public void visit(CondStmtNode node) {
		super.visit(node);
		if(!(node.getCond().getType() instanceof BoolType)) {
			throw new SemanticError("BoolType Condition Expected", node.location());
		}
	}

	@Override
	public void visit(WhileStmtNode node) {
		++inLoop;
		super.visit(node);
		if(!(node.getCond().getType() instanceof BoolType)) {
			throw new SemanticError("BoolType Condition Expected", node.location());
		}
		--inLoop;
	}

	@Override
	public void visit(ForStmtNode node) {
		++inLoop;
		super.visit(node);
		if(!(node.getCond().getType() instanceof BoolType)) {
			throw new SemanticError("BoolType Condition Expected", node.location());
		}
		--inLoop;
	}

	@Override
	public void visit(BreakStmtNode node) {
		if(inLoop <= 0) throw new SemanticError("Loop Expected", node.location());
	}

	@Override
	public void visit(ContinueStmtNode node) {
		if(inLoop <= 0) throw new SemanticError("Loop Expected", node.location());
	}

	@Override
	public void visit(ReturnStmtNode node) {
		if(currentReturnType == null || currentReturnType instanceof VoidType) {
			if(node.getExpr() != null) throw new SemanticError("Unexpected ReturnValue", node.location());
		}
		else {
			if(node.getExpr() == null) throw new SemanticError("ReturnValue Expected", node.location());
			else {
				node.getExpr().accept(this);

				if (node.getExpr().getType() instanceof NullLiteral) {
					if (!(currentReturnType instanceof ArrayType || currentReturnType instanceof ClassType))
						throw new SemanticError("Unexpected ReturnValue", node.location());
				}
				else {
					if (!(node.getExpr().getType().equals(currentReturnType)))
						throw new SemanticError("Unexpected ReturnValue", node.location());
				}
			}
		}
	}

	@Override
	public void visit(SuffixExprNode node) {
		node.getExpr().accept(this);

		if(!(node.getExpr().isLeftValue() && node.getExpr().getType() instanceof IntType)) {
			throw new SemanticError("IntType LeftValue Expected", node.location());
		}

		node.setType(IntType.getInstance());
		node.setLeftValue(false);
	}

	@Override
	public void visit(FuncCallExprNode node) {
		node.getFunc().accept(this);

		if(!(node.getFunc() instanceof EntityExprNode))
			throw new SemanticError("Entity Expected", node.location());
		if(!(node.getFunc().getType() instanceof FunctionType))
			throw new SemanticError("Function expected", node.location());

		FuncEntity funcEntity = (FuncEntity) ((EntityExprNode) node.getFunc()).getEntity();

		node.setFuncEntity(funcEntity);
		int postfix = funcEntity.isMember() ? 1 : 0;

		if(funcEntity.getParameters().size() - postfix != node.getArgs().size()) {
			throw new SemanticError("Incorrect number of Args", node.location());
		}

		for(int i = 0; i < node.getArgs().size(); i++) {
			VarEntity varEntity = funcEntity.getParameters().get(i + postfix);
			ExprNode exprNode = node.getArgs().get(i);

			exprNode.accept(this);

			if(exprNode.getType() instanceof NullLiteral) {
				if(!(varEntity.getType() instanceof ArrayType || varEntity.getType() instanceof ClassType))
					throw new SemanticError("Unexpected null of Args", node.location());
			}
			else {
				if(!(exprNode.getType().equals(varEntity.getType())))
					throw new SemanticError(String.format("Can't match Type at %d(th) Arg", i + 1),
											node.location());
			}
		}

		node.setType(funcEntity.getReturnType());
		node.setLeftValue(false);
	}

	@Override
	public void visit(SubscriptExprNode node) {
		node.getArray().accept(this);
		if(!(node.getArray().getType() instanceof ArrayType))
			throw new SemanticError("ArrayType Expected", node.location());
		node.getPostfix().accept(this);
		if(!(node.getPostfix().getType() instanceof IntType))
			throw new SemanticError("IntType Expected", node.location());
		node.setType(((ArrayType) node.getArray().getType()).getBaseType());
		node.setLeftValue(true);
	}

	private ClassType getClassType(Type type) {
		if(type instanceof StringType) return new ClassType(STR_CLASS_NAME);
		if(type instanceof ArrayType) return new ClassType(ARRAY_CLASS_NAME);
		if(type instanceof ClassType) return (ClassType) type;
		return null;
	}

	@Override
	public void visit(MemberAccessExprNode node) {
		node.getExpr().accept(this);

		ClassType classType = getClassType(node.getExpr().getType());

		if(classType == null)
			throw new SemanticError("ClassType Expected", node.location());

		ClassEntity classEntity = (ClassEntity) currentScope.find(CLASS_PREFIX + classType.getName());

		if(classEntity == null) throw new SemanticError("ClassType Not Found", node.location());

		Entity memberEntity = classEntity.getScope().scratch(VAR_PREFIX + node.getMember());
		if(memberEntity == null) {
			memberEntity = classEntity.getScope().scratch(FUNC_PREFIX + node.getMember());
		}

		if(memberEntity == null) {
			throw new SemanticError(String.format("Can't Recognize %s", node.getMember()), node.location());
		}

		node.setEntity(memberEntity);
		node.setType(memberEntity.getType());
		node.setLeftValue(true);
	}

	@Override
	public void visit(PrefixExprNode node) {
		node.getExpr().accept(this);
		switch (node.getOp()) {
			case DEC:
			case INC:
				if (!(node.getExpr().getType() instanceof IntType && node.getExpr().isLeftValue()))
					throw new SemanticError("IntType LeftValue Expect", node.location());
				node.setType(IntType.getInstance());
				node.setLeftValue(true);
				break;
			case NEG:
			case POS:
			case BIT_NOT:
				if (!(node.getExpr().getType() instanceof IntType))
					throw new SemanticError("IntType Expect", node.location());
				node.setType(IntType.getInstance());
				node.setLeftValue(false);
				break;
			case LOG_NOT:
				if (!(node.getExpr().getType() instanceof BoolType))
					throw new SemanticError("BoolType Expect", node.location());
				node.setType(BoolType.getInstance());
				node.setLeftValue(false);
				break;
			default:
				assert false;
		}
	}

	@Override
	public void visit(NewExprNode node) {
		if(node.getDims() != null) {
			for(ExprNode dim : node.getDims()) {
				dim.accept(this);
				if(!(dim.getType() instanceof IntType))
					throw new SemanticError("IntType dimension Expected", dim.location());
			}
		}

		Type nType = node.getNewType().getType();

		if(nType instanceof ClassType) {
			Entity entity = currentScope.find(CLASS_PREFIX + ((ClassType) nType).getName());

			if(entity instanceof ClassEntity) {
				node.setClassSize(((ClassEntity) entity).getWidth());
			}
			else {
				throw new SemanticError("Can't find ClassType", node.location());
			}
		}
		node.setType(nType);
		node.setLeftValue(false);
	}

	@Override
	public void visit(BinaryExprNode node) {
		ExprNode L = node.getLhs();
		ExprNode R = node.getRhs();

		L.accept(this);
		R.accept(this);

		SemanticError error = new SemanticError("Can't compare these two Expr", node.location());

		switch (node.getOp()) {
			case EQ:
			case NEQ:
				if(!L.getType().equals(R.getType())) {
					int cnt = (L.getType() instanceof ClassType ||
							L.getType() instanceof ArrayType ||
							L.getType() instanceof NullLiteral ? 1 : 0) +
							(R.getType() instanceof ClassType ||
							R.getType() instanceof ArrayType ||
							R.getType() instanceof NullLiteral ? 1 : 0);

					if(cnt == 0 || cnt == 1) throw error;
					if(!(L.getType() instanceof NullLiteral || R.getType() instanceof NullLiteral)) throw error;
				}
				node.setType(BoolType.getInstance());
				node.setLeftValue(false);
				break;
			case GEQ:
			case LEQ:
			case GT:
			case LT:
				if (!L.getType().equals(R.getType()) ||
						!(L.getType() instanceof IntType || L.getType() instanceof StringType)) throw error;
				node.setType(BoolType.getInstance());
				node.setLeftValue(false);
				break;
			case ADD:
				if(L.getType().equals(R.getType()) &&
					L.getType() instanceof StringType) {
					node.setType(StringType.getInstance());
					node.setLeftValue(false);
					break;
				}
			case DIV:
			case MOD:
			case MUL:
			case SHL:
			case SHR:
			case SUB:
			case BIT_OR:
			case BIT_AND:
			case BIT_XOR:
				if (!L.getType().equals(R.getType()) ||
						!(L.getType() instanceof IntType)) throw error;
				node.setType(IntType.getInstance());
				node.setLeftValue(false);
				break;
			case LOG_OR:
			case LOG_AND:
				if (!L.getType().equals(R.getType()) ||
						!(L.getType() instanceof BoolType)) throw error;
				node.setType(BoolType.getInstance());
				node.setLeftValue(false);
				break;
			default: assert false;
		}
	}

	@Override
	public void visit(AssignExprNode node) {
		ExprNode L = node.getLhs();
		ExprNode R = node.getRhs();

		L.accept(this);
		R.accept(this);

		if(!L.isLeftValue()) throw new SemanticError("Invalid LeftValue", node.location());

		if(!L.getType().equals(R.getType()) &&
			!(R.getType() instanceof NullLiteral && (L.getType() instanceof ClassType || L.getType() instanceof ArrayType))) {
			throw new SemanticError("Invalid RightValue", node.location());
		}

		node.setType(L.getType());
		node.setLeftValue(true);
	}

	@Override
	public void visit(IdentifierExprNode node) {
		String name = node.getID();
		Entity entity = currentScope.casualFind(name);
		if(entity instanceof VarEntity) {
			node.setEntity(entity);
			node.setType(entity.getType());
			node.setLeftValue(true);
		}
		else if(entity instanceof FuncEntity) {
			node.setEntity(entity);
			node.setType(entity.getType());
			node.setLeftValue(false);
		}
		else{
			throw new SemanticError("Unexpected identifier", node.location());
		}
	}

	@Override
	public void visit(ThisExprNode node) {
		Entity entity = currentScope.find(VAR_PREFIX + THIS_NAME);
		if(entity == null) throw new SemanticError("Can't find \"this\"", node.location());
		node.setEntity(entity);
		node.setType(entity.getType());
		node.setLeftValue(false);
	}

	@Override
	public void visit(StringConstNode node) {
		node.setType(StringType.getInstance());
		node.setLeftValue(false);
	}

	@Override
	public void visit(IntConstNode node) {
		node.setType(IntType.getInstance());
		node.setLeftValue(false);
	}

	@Override
	public void visit(BoolConstNode node) {
		node.setType(BoolType.getInstance());
		node.setLeftValue(false);
	}

	@Override
	public void visit(NullLiteralNode node) {
		node.setType(NullLiteral.getInstance());
		node.setLeftValue(false);
	}

	@Override
	public void visit(TypeNode node) {
		super.visit(node);
	}
}
