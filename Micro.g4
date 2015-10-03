grammar Micro;

@members {
    SymbolTable currTable = null;
    SymbolTableStack symbolStack = new SymbolTableStack();
}

/* Program */
program  returns [SymbolTable table]
    : PROGRAM id BEGIN pgm_body END {$table = $pgm_body.table;};
id                : IDENTIFIER;
pgm_body returns [SymbolTable table]
    : {currTable = new Program();}
    decl func_declarations {
       $table = currTable;
    };
decl : string_decl decl | var_decl decl | /* empty */;

/* Global String Declaration */
string_decl : STRING id ASSIGN str SEMI {
        SymbolEntry entry = new SymbolEntry($id.text, "STRING", $str.text);
        currTable.addElement(entry);
    };
str               : STRINGLITERAL;

/* Variable Declaration */
var_decl
    : var_type id_list SEMI {
        String[] names = $id_list.text.split(",");
        for (String name : names) {
            currTable.addElement(new SymbolEntry(name, $var_type.text));
        }
    };
var_type	      : FLOAT | INT;
any_type          : var_type | VOID;
id_list           : id id_tail;
id_tail           : COMMA id id_tail   | /* empty */;

/* Function Paramater List */
param_decl_list   : param_decl param_decl_tail | /* empty */;
param_decl        : var_type id {
        currTable.addElement(new SymbolEntry($id.text, $var_type.text));
    };
param_decl_tail   : COMMA param_decl param_decl_tail | /* empty */;

/* Function Declarations */
func_declarations : func_decl func_declarations | /* empty */;
func_decl
    : {
        symbolStack.push(currTable);
        currTable = new Function(currTable);
    } FUNCTION any_type id LPAREN param_decl_list RPAREN BEGIN func_body END {
        currTable.setScope($id.text);
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();
    };
func_body         : decl stmt_list;

/* Statement List */
stmt_list         : stmt stmt_list | /* empty */;
stmt              : base_stmt | if_stmt | for_stmt | COMMENT;
base_stmt         : assign_stmt | read_stmt | write_stmt | return_stmt;

/* Basic Statements */
assign_stmt       : assign_expr SEMI;
assign_expr       : id ASSIGN expr ;
read_stmt         : READ LPAREN id_list RPAREN SEMI;
write_stmt        : WRITE LPAREN id_list RPAREN SEMI;
return_stmt       : RETURN expr SEMI;

/* Expressions */
expr              : expr_prefix factor;
expr_prefix       : expr_prefix factor addop | /* empty */;
factor            : factor_prefix postfix_expr;
factor_prefix     : factor_prefix postfix_expr mulop | /* empty */;
postfix_expr      : primary | call_expr;
call_expr         : id LPAREN expr_list RPAREN;
expr_list         : expr expr_list_tail | /* empty */;
expr_list_tail    : COMMA expr expr_list_tail | /* empty */;
primary           : LPAREN expr RPAREN | id | INTLITERAL | FLOATLITERAL;
addop             : '+' | '-';
mulop             : '*' | '/';

/* Complex Statements and Condition */
if_stmt           : IF LPAREN cond RPAREN decl stmt_list else_part FI;
else_part         : ELSE decl stmt_list | /* empty */;
cond              : expr compop expr;
compop            : '<' | '>' | '=' | '!=' | '<=' | '>=';

init_stmt         : assign_expr | /* empty */;
incr_stmt         : assign_expr | /* empty */;


/* ECE 573 students use this version of for_stmt */
for_stmt       : FOR LPAREN init_stmt SEMI cond SEMI incr_stmt RPAREN decl aug_stmt_list ROF;

/* CONTINUE and BREAK statements. ECE 573 students only */
aug_stmt_list     : aug_stmt aug_stmt_list | /* empty */;
aug_stmt          : base_stmt | aug_if_stmt | for_stmt | CONTINUE SEMI | BREAK SEMI;

/* Augmented IF statements for ECE 573 students */
aug_if_stmt       : IF LPAREN cond RPAREN decl aug_stmt_list aug_else_part FI;
aug_else_part     : ELSE decl aug_stmt_list | /* empty */;



//empty:;

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

IDENTIFIER: [a-zA-Z][a-zA-Z0-9]*;
INTLITERAL: [0-9]+;
FLOATLITERAL: [0-9]+.[0-9]+;
STRINGLITERAL:'"'~["]*'"';
COMMENT: '--'~[\r\n]* -> skip;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

