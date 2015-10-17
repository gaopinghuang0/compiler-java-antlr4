grammar Micro;

@header {
    import java.util.*;
}

@members {
    SymbolTable currTable = null;
    SymbolTableStack symbolStack = new SymbolTableStack();
    ArrayList<Code> codeList = new ArrayList<>();
}

/* Program */
program  returns [ReturnData data]
    : PROGRAM id BEGIN pgm_body END {
        ReturnData data = new ReturnData();
        data.setTable($pgm_body.table);
        data.setCodeList(codeList);
        $data = data;
    };
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
assign_expr       : id ASSIGN expr {
        String type = $expr.code.getType();
        String opcode = "";
        if (type != null && type.equals("INT")) {
            opcode = "STOREI";
        } else if (type != null && type.equals("FLOAT")) {
            opcode = "STOREF";
        } else {
            System.out.println($expr.text);
        }
        Code code = new TwoAddressCode(opcode, $expr.code.getResult(), $id.text, type);
        codeList.add(code);
    };
read_stmt         : READ LPAREN id_list RPAREN SEMI;
write_stmt        : WRITE LPAREN id_list RPAREN SEMI;
return_stmt       : RETURN expr SEMI;

/* Expressions */
expr  returns [Code code]
    : expr_prefix factor {
        Code e = $expr_prefix.code;
        Code f = $factor.code;
        String type = f.getType();
        if (e != null && type != null) {
            String op = e.getOpcode();
            $code = new ThreeAddressCode(op, e.getResult(), f.getResult(), type);
            codeList.add($code);
        } else {
            $code = f;
        }
    };
expr_prefix  returns [Code code]
    : expr_prefix factor addop {
        Code last = null;
        String lastOp = null;
        String type = $factor.code.getType();

        if (codeList.size() != 0) {
            last = codeList.get(codeList.size()-1);
            lastOp = last.getOpcode();
        }
        System.out.println("in expr_prefix    "+ last.getClass()+last.toIR()+lastOp);
        if (last != null && lastOp != null && (lastOp.startsWith("MULT") || lastOp.startsWith("DIV"))) {
                String op = "";
                if ($addop.text.equals("+")) {
                    if (type.equals("INT")) op = "ADDI";
                    else op = "ADDF";
                } else if ($addop.text.equals("-")) {
                    if (type.equals("INT")) op = "SUBI";
                    else op = "SUBF";
                } else {
                    System.out.println("unknown");
                }
            $code = new ThreeAddressCode(op, last.getResult(), $factor.code.getResult(), type);
            codeList.add($code);
        } else {
            System.out.println("I am here   "+$factor.text);
            String op = "";
            if ($addop.text.equals("+")) {
                if (type.equals("INT")) op = "ADDI";
                else op = "ADDF";
            } else if ($addop.text.equals("-")) {
                if (type.equals("INT")) op = "SUBI";
                else op = "SUBF";
            } else {
                System.out.println("unknown");
            }
            $code = new OneAddressCode(op, $factor.code.getResult(), type);
            System.out.println($code.getClass()+$code.toIR());
        }
    }| /* empty */;
factor  returns [Code code]
    : factor_prefix postfix_expr {
        Code f = $factor_prefix.code;
        Code p = $postfix_expr.code;
        String type = p.getType();
        if (f != null && type != null) {
            String op = f.getOpcode();
            $code = new ThreeAddressCode(op, f.getResult(), p.getResult(), type);
            codeList.add($code);
        } else {
            $code = p;
        }
    };
factor_prefix  returns [Code code]
    : factor_prefix postfix_expr mulop {
        Code last = null;
        String lastOp = null;
        String type = $postfix_expr.code.getType();

        if (codeList.size() != 0) {
            last = codeList.get(codeList.size()-1);
            lastOp = last.getOpcode();
        }
        System.out.println(last.getClass()+last.toIR()+lastOp);

        System.out.println($mulop.text);

        if (last != null && lastOp != null && (lastOp.startsWith("MULT") || lastOp.startsWith("DIV"))) {
            $code = new ThreeAddressCode(lastOp, last.getResult(), $postfix_expr.code.getResult(), type);
            System.out.println($code.getClass()+$code.toIR());
            codeList.add($code);
        } else {
            String op = "";
            if ($mulop.text.equals("*")) {
                if (type.equals("INT")) op = "MULTI";
                else op = "MULTF";
            } else {
                if (type.equals("INT")) op = "DIVI";
                else op = "DIVF";
            }
            $code = new OneAddressCode(op, $postfix_expr.code.getResult(), type);
            System.out.println($code.getClass()+$code.toIR());
        }

    }| /* empty */;
postfix_expr  returns [Code code]
    : primary {
        $code = $primary.code;
    }
    | call_expr {
    //TODO: in next step
    };
call_expr         : id LPAREN expr_list RPAREN;
expr_list         : expr expr_list_tail | /* empty */;
expr_list_tail    : COMMA expr expr_list_tail | /* empty */;
primary  returns [Code code]
    : LPAREN expr RPAREN {
        $code = $expr.code;
    }
    | id {
        // TODO: look up type from currTable to it's parent
        // type = currTable.lookup($id.text);
        $code = new OneAddressCode($id.text, "INT");
    }
    | INTLITERAL {
        $code = new TwoAddressCode("STOREI", $INTLITERAL.text, "INT");
        codeList.add($code);
    }
    | FLOATLITERAL {
        $code = new TwoAddressCode("STOREF", $FLOATLITERAL.text, "FLOAT");
        codeList.add($code);
    };
addop             : '+' | '-';
mulop             : '*' | '/';

/* Complex Statements and Condition */
if_stmt
    : {
        symbolStack.push(currTable);
        currTable = new Block(currTable);
    } IF LPAREN cond RPAREN decl stmt_list else_part FI {
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();
    };
else_part
    : {
        symbolStack.push(currTable);
        currTable = new Block(currTable);
    } ELSE decl stmt_list {
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();
    }| /* empty */;
cond              : expr compop expr;
compop            : '<' | '>' | '=' | '!=' | '<=' | '>=';

init_stmt         : assign_expr | /* empty */;
incr_stmt         : assign_expr | /* empty */;


/* ECE 573 students use this version of for_stmt */
for_stmt
    : {
        symbolStack.push(currTable);
        currTable = new Block(currTable);
    } FOR LPAREN init_stmt SEMI cond SEMI incr_stmt RPAREN decl aug_stmt_list ROF {
       currTable.getParent().addChild(currTable);
       currTable = symbolStack.pop();
    };

/* CONTINUE and BREAK statements. ECE 573 students only */
aug_stmt_list     : aug_stmt aug_stmt_list | /* empty */;
aug_stmt          : base_stmt | aug_if_stmt | for_stmt | CONTINUE SEMI | BREAK SEMI;

/* Augmented IF statements for ECE 573 students */
aug_if_stmt
    : {
         symbolStack.push(currTable);
         currTable = new Block(currTable);
    } IF LPAREN cond RPAREN decl aug_stmt_list aug_else_part FI {
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();
    };
aug_else_part
    : {
         symbolStack.push(currTable);
         currTable = new Block(currTable);
    } ELSE decl aug_stmt_list {
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();
    }| /* empty */;



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

