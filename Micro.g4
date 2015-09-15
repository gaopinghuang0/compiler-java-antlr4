grammar Micro
/* PROGRAM */
/* Program */
program           : PROGRAM id BEGIN pgm_body END ;
id                : IDENTIFIER;
pgm_body          : decl func_declarations;
decl		        : string_decl decl | var_decl decl | empty;

/* Global String Declaration */
string_decl       : STRING id ASSIGN str SEMI;
str               : STRINGLITERAL;
comment:    COMMENT;

/* Variable Declaration */
var_decl          : var_type id_list SEMI;
var_type	      : FLOAT | INT;
any_type          : var_type | VOID;
id_list           : id id_tail;
id_tail           : COMMA id id_tail | empty;

/* Function Paramater List */
param_decl_list   : param_decl param_decl_tail | empty;
param_decl        : var_type id;
param_decl_tail   : COMMA param_decl param_decl_tail | empty;

/* Function Declarations */
func_declarations : func_decl func_declarations | empty;
func_decl         : FUNCTION any_type id LPAREN param_decl_list RPAREN BEGIN func_body END;
func_body         : decl stmt_list;

/* Statement List */
stmt_list         : stmt stmt_list | empty;
stmt              : base_stmt | if_stmt | for_stmt | comment;
base_stmt         : assign_stmt | read_stmt | write_stmt | return_stmt;

/* Basic Statements */
assign_stmt       : assign_expr SEMI;
assign_expr       : id ASSIGN expr ;
read_stmt         : READ LPAREN id_list RPAREN SEMI;
write_stmt        : WRITE LPAREN id_list RPAREN SEMI;
return_stmt       : RETURN expr SEMI;

/* Expressions */
expr              : expr_prefix factor;
expr_prefix       : expr_prefix factor addop | empty;
factor            : factor_prefix postfix_expr;
factor_prefix     : factor_prefix postfix_expr mulop | empty;
postfix_expr      : primary | call_expr;
call_expr         : id LPAREN expr_list RPAREN;
expr_list         : expr expr_list_tail | empty;
expr_list_tail    : COMMA expr expr_list_tail | empty;
primary           : LPAREN expr RPAREN | id | INTLITERAL | FLOATLITERAL;
addop             : '+' | '-';
mulop             : '*' | '/';

/* Complex Statements and Condition */
if_stmt           : IF LPAREN cond RPAREN decl stmt_list else_part FI;
else_part         : ELSE decl stmt_list | empty;
cond              : expr compop expr;
compop            : '<' | '>' | '=' | '!=' | '<=' | '>=';

init_stmt         : assign_expr | empty;
incr_stmt         : assign_expr | empty;


/* ECE 573 students use this version of for_stmt */
for_stmt       : FOR LPAREN init_stmt SEMI cond SEMI incr_stmt RPAREN decl aug_stmt_list ROF;

/* CONTINUE and BREAK statements. ECE 573 students only */
aug_stmt_list     : aug_stmt aug_stmt_list | empty;
aug_stmt          : base_stmt | aug_if_stmt | for_stmt | CONTINUE SEMI | BREAK SEMI;

/* Augmented IF statements for ECE 573 students */
aug_if_stmt       : IF LPAREN cond RPAREN decl aug_stmt_list aug_else_part FI;
aug_else_part     : ELSE decl aug_stmt_list | empty;



empty: ;

PROGRAM:'PROGRAM';
BEGIN:'BEGIN';
END:'END';
FUNCTION:'FUNCTION';
READ:'READ';
WRITE:'WRITE';
IF:'IF';
ELSE:'ELSE';
FI:'FI';
FOR:'FOR';
ROF:'ROF';
CONTINUE:'CONTINUE';
BREAK:'BREAK';
RETURN:'RETURN';
INT:'INT';
VOID:'VOID';
STRING:'STRING';
FLOAT:'FLOAT';




//OPERATORS:':=' | '+' | '-' | '*' | '/' | '=' | '!=' | '<' | '>' | '(' | ')' | ';' | ',' | '<=' | '>=';

//assign_op: OPERATORS;
//add_g:OPERATORS;
//sub_g:OPERATORS;
//mul_g:OPERATORS;
//div_g:OPERATORS;
//eq_g:OPERATORS;
//not_eq_g:OPERATORS;
//less_g:OPERATORS;
//more_g:OPERATORS;
//l_par_g:OPERATORS;
//r_par_g:OPERATORS;
//semi_g:OPERATORS;
//comma_g:OPERATORS;
//less_eq_g:OPERATORS;
//more_eq_g:OPERATORS;
ASSIGN: ':=';
ADD:'+';
SUB:'-';
MUL:'*';
DIV:'/';
EQUAL:'=';
NOTEQUAL:'!=';
LESS:'<';
MORE:'>';
LPAREN:'(';
RPAREN:')';
SEMI:';';
COMMA:',';
LE:'<=';
GE:'>=';


//add_g:'+';
//sub_g:'-';
//mul_g:'*';
//div_g:'/';
//eq_g:'=';
//not_eq_g:'!=';
//less_g:'<';
//more_g:'>';
//l_par_g:'(';
//r_par_g:')';
//semi_g:';';
//comma_g:',';
//less_eq_g:'<=';
//more_eq_g:'>=';
//KEYWORD:'';
//KEYWORD: 'PROGRAM'|'BEGIN'|'END'|'FUNCTION'|'READ'|'WRITE'|'IF'|'ELSE'|'FI'|'FOR'|'ROF'|'CONTINUE'|'BREAK'|'RETURN'|'INT'|'VOID'|'STRING'|'FLOAT' ;
//KEYWORD: PROGRAM | BEGIN | END | FUNCTION | READ | WRITE | IF | ELSE | FI | FOR | CONTINUE | BREAK | RETURN | INT | VOID | STRING | FLOAT;
IDENTIFIER: [a-zA-Z][a-zA-Z0-9]*;
INTLITERAL: [0-9]+;
FLOATLITERAL: [0-9]+.[0-9]+;
//STRINGLITERAL: (~'"') | '"'('a'..'z' | 'A'..'Z')*'"';
//STRINGLITERAL:'"' ('a'..'z'|'A'..'Z'|'\n'|'\t'|'\\'|EOF|'\r'|OPERATORS )+'"';
STRINGLITERAL:'"'~["]*'"';
//COMMENT:(('--')('a'..'z' | 'A'..'Z')* '--') | (('--')('a'..'z' | 'A'..'Z')*);
COMMENT: '--'~[\r\n]* -> skip;


//
//PROGRAM:'PROGRAM';
//BEGIN:'BEGIN';
//END:'END';
//FUNCTION:'FUNCTION';
//READ:'READ';
//WRITE:'WRITE';
//IF:'IF';
//ELSE:'ELSE';
//FI:'FI';
//FOR:'FOR';
//ROF:'ROF';
//CONTINUE:'CONTINUE';
//BREAK:'BREAK';
//RETURN: 'RETURN';
//INT:'INT';
//VOID:'VOID';
//STRING:'STRING';
//FLOAT:'FLOAT';


WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
//WS : ( ' ' | '\t' | '\r' | '\n' )+ { $channel = HIDDEN; };

