expression
	:	assignmentExpression
	|	expression ',' assignmentExpression
	;

assignmentExpression
	:	logicalOrExpression
	|	unaryExpression '=' assignmentExpression
	;

logicalOrExpression
	:	logicalAndExpression
	|	logicalOrExpression '||' logicalAndExpression
	;

logicalAndExpression
	:	inclusiveOrExpression
	|	logicalAndExpression '&&' inclusiveOrExpression
	;
	
inclusiveOrExpression
	:	exclusiveOrExpression
	|	inclusiveOrExpression '|' exclusiveOrExpression
	;

exclusiveOrExpression
	:	andExpression
	|	exclusiveOrExpression '^' andExpression 
	;
	
andExpression
	:	equalityExpression
	|	andExpression '&' equalityExpression 
	;

equalityExpression
	:	relationalExpression
	|	equalityExpression '==' relationalExpression
	|	equalityExpression '!=' relationalExpression
	;

relationalExpression
	:	shiftExpression
	|	relationalExpression '<' shiftExpression
	|	relationalExpression '>' shiftExpression
	|	relationalExpression '<=' shiftExpression
	|	relationalExpression '>=' shiftExpression
	;

shiftExpression
	:	additiveExpression
	|	shiftExpression '<<' additiveExpression
	|	shiftExpression '>>' additiveExpression	
	;

additiveExpression
	:	multiplicativeExpression
	|	additiveExpression '+' multiplicativeExpression
	|	additiveExpression '-' multiplicativeExpression
	;

multiplicativeExpression
	:	unaryExpression
	|	multiplicativeExpression '*' unaryExpression
	|	multiplicativeExpression '/' unaryExpression
	|	multiplicativeExpression '%' unaryExpression
	;
	
unaryExpression
	:	posfixExpression
	|	'++' unaryExpression
	|	'--' unaryExpression
	|	unaryOperator unaryExpression
	;

unaryOperator
	:	'+'
    |	'-'
    |	'~'
    |	'!'
    ;
    	
posfixExpression
	:	primaryExpression
	|	posfixExpression '++'
	|	posfixExpression '--'
	|	posfixExpression '.' ID
	|	posfixExpression '[' expression ']'
	|	posfixExpression '(' parameterList ')'
	|	NEW creator
	;


