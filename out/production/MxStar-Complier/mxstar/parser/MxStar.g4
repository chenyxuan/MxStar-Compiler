grammar MxStar;

@header{package mxstar.parser;}

translationUnit
	:	externalDeclaration* EOF
	;

// declaration 
externalDeclaration
	:	functionDefinition
	|	classDeclaration
	|	variableDeclaration
	;
	
functionDefinition
	:	typeSpecifier? ID '(' parameterDeclarationList? ')' blockStatement
	;

parameterDeclarationList
	:	parameterDeclaration (',' parameterDeclaration)*
	;

parameterDeclaration
	:	typeSpecifier ID
	;

classDeclaration
	:	CLASS ID '{' (functionDefinition | variableDeclaration)* '}'
	;
	
variableDeclaration
	:	typeSpecifier variableDeclarationList ';'
	;

variableDeclarationList
	:	variableDeclarator (',' variableDeclarator)*
	;

variableDeclarator
	:	ID ('=' expression)?
	;
	
// trivial
typeSpecifier
	:	nonVoidType
	|	VOID
	;

nonVoidType
	:	nonArrayType ('[' ']')+  # arrayType
	|	nonArrayType             # otherType
	;
		
nonArrayType
    :   INT
    |   BOOL
    |   STRING
	|	ID
	;
	
// statement
statement
	:	expression ';'
	|	blockStatement
	|	conditionStatement
	|	loopStatement
	|	jumpStatement
	|	';'
	;

blockStatement
	:	'{' blockCompound* '}'
	;

blockCompound
	:	statement
	|	variableDeclaration
	;
	
conditionStatement
	:	IF '(' expression ')'  thenStmt = statement (ELSE elseStmt = statement)?
	;

loopStatement
	:	WHILE '(' expression ')' statement	#whileStmt
	|	FOR '(' init = expression? ';' cond = expression? ';' step = expression? ')' statement	#forStmt
	;
	
jumpStatement
	:	RETURN expression? ';'	# returnStmt
	|	BREAK ';'				# breakStmt
	|	CONTINUE ';'			# continueStmt
	;

// expression (rewrite)
expression
    :   primaryExpression                                       		# primaryExpr
    |	expression op = ('++' | '--')                            		# suffixExpr
    |   expression '.' ID		                               			# memberAccessExpr
    |   array = expression '[' postfix = expression ']'          		# subscriptExpr
    |   expression '(' parameterList? ')'                     			# funcCallExpr
    |   <assoc = right> NEW creator                             		# newExpr
    |   <assoc = right> op = ('++'|'--') expression                 	# prefixExpr //unaryExpression
    |   <assoc = right> op = ('+' | '-') expression                 	# prefixExpr 
    |   <assoc = right> op = ('!' | '~') expression                 	# prefixExpr
    |   lhs = expression op = ('*' | '/' | '%') rhs = expression      		# binaryExpr // mathematicExpression
    |   lhs = expression op = ('+' | '-') rhs = expression            		# binaryExpr
    |   lhs = expression op = ('<<'|'>>') rhs = expression            		# binaryExpr // shiftExpression
    |   lhs = expression op = ('<' | '>') rhs = expression            		# binaryExpr // relationalExpression
    |   lhs = expression op = ('<='|'>=') rhs = expression            		# binaryExpr
    |   lhs = expression op = ('=='|'!=') rhs = expression            		# binaryExpr
    |   lhs = expression op = '&' rhs = expression                    		# binaryExpr // bitwiseExpression
    |   lhs = expression op = '^' rhs = expression                    		# binaryExpr
    |   lhs = expression op = '|' rhs = expression                    		# binaryExpr
    |   <assoc = right> lhs = expression op = '&&' rhs = expression     	# binaryExpr // logicalExpression
    |   <assoc = right> lhs = expression op = '||' rhs = expression     	# binaryExpr
    |   <assoc = right> lhs = expression op = '=' rhs = expression      	# assignExpr // assignmentExpression
    ;
	
primaryExpression
    :   ID                  # IdentifierExpr
    |   THIS                # thisExpr
    |   constant            # constExpr
    |   '(' expression ')'  # subExpr
    ;

parameterList
    :   expression (',' expression)*
    ;

//errorCreator is to handle the conflict between arrayCreator and subscriptExpr
creator
    :   nonArrayCreator ('[' expression ']')+ ('[' ']')+ ('[' expression ']')+	# errorCreator
    |   nonArrayCreator ('[' expression ']')+ ('[' ']')*                        # arrayCreator
    |   nonArrayCreator                                                        	# otherCreator
    ;

nonArrayCreator
    :   INT
    |   BOOL
    |   STRING
    |   ID ('(' ')')?
    ;

constant
    :   INTEGER_CONST	# intConst
    |   STRING_CONST    # stringConst
    |   BOOL_CONST		# boolConst
    |	NULL         	# nullLiteral
    ;

// constant
INTEGER_CONST
    :   [1-9] [0-9]*
    |   '0'
    ;

STRING_CONST
    :   '"' CHARCONST* '"'
    ;

fragment CHARCONST
    :   ~["\\\r\n]
    |   '\\' ["n\\]
    ;

BOOL_CONST
    :   'true'
    |   'false'
    ;

// reserved keywords
BOOL	:	'bool'	;
INT		:	'int'	;
STRING	:	'string';
NULL	:	'null'	;
VOID	:	'void'	;
IF		:	'if'	;
ELSE	:	'else'	;
FOR		:	'for'	;
WHILE	:	'while'	;
BREAK	:	'break'	;
CONTINUE:	'continue'	;
RETURN	:	'return';
NEW		:	'new'	;
CLASS	:	'class'	;
THIS	:	'this'	;

// identifier
ID	:	[a-zA-Z]([a-zA-Z0-9_])*;

// skip	
NEWLINE	:	'\r'? '\n' -> skip;
WHITESPACES	:	[ \t]+ -> skip;
LINE_COMMENT	:   '//' ~[\r\n]* -> skip;
BLOCK_COMMENT:   '/*' .*? '*/' -> skip;

