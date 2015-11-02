grammar Micro;

@header {
    import java.util.*;
}

@members {
    SymbolTable currTable = null;
    CostomStack<SymbolTable> symbolStack = new CostomStack<>();
    ArrayList<Code> codeList = new ArrayList<>();
    CostomStack<Graph> graphStack = new CostomStack<>();
    Graph currGraph = null;
    // handle break and continue
    // ArrayList<Graph> tempForGraphList = new ArrayList<>();
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
read_stmt
    : READ LPAREN id_list RPAREN SEMI {
       String[] names = $id_list.text.split(",");
       for (String name : names) {
           String type = currTable.lookUpType(name);
           String op = type.equals("INT") ? "READI" : "READF";
           Code c = new OneAddressCode(op, name, type);
           codeList.add(c);
       }
    };
write_stmt
    : WRITE LPAREN id_list RPAREN SEMI {
       String[] names = $id_list.text.split(",");
       for (String name : names) {
           String type = currTable.lookUpType(name);
           String op = type.equals("INT") ? "WRITEI" : "WRITEF";
           Code c = new OneAddressCode(op, name, type);
           codeList.add(c);
       }
    };
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
    : ep=expr_prefix factor addop {
        String type = $factor.code.getType();
        String op = "";
        if ($addop.text.equals("+")) {
            op = type.equals("INT") ? "ADDI" : "ADDF";
        } else {
            op = type.equals("INT") ? "SUBI" : "SUBF";
        }
        if ($ep.code != null) {
            $code = new ThreeAddressCode($ep.code.getOpcode(), $ep.code.getResult(),
                    $factor.code.getResult(), type);
            codeList.add($code);
            $code = new OneAddressCode(op, $code.getResult(), type);
        } else {
            $code = new OneAddressCode(op, $factor.code.getResult(), type);
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
    : fp=factor_prefix postfix_expr mulop {
        String type = $postfix_expr.code.getType();
        String op = "";
        if ($mulop.text.equals("*")) {
            op = type.equals("INT") ? "MULTI" : "MULTF";
        } else {
            op = type.equals("INT") ? "DIVI" : "DIVF";
        }
        if ($fp.code != null) {
            $code = new ThreeAddressCode($fp.code.getOpcode(), $fp.code.getResult(),
                    $postfix_expr.code.getResult(), type);
            codeList.add($code);
            $code = new OneAddressCode(op, $code.getResult(), type);
        } else {
            $code = new OneAddressCode(op, $postfix_expr.code.getResult(), type);
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
        // look up type from currTable to it's parent
        String type = currTable.lookUpType($id.text);
        $code = new OneAddressCode($id.text, type);
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
        graphStack.push(currGraph);
        currGraph = new IfGraph();
    } IF LPAREN cond RPAREN decl stmt_list {
        OneAddressCode midCode = new OneAddressCode("JUMP", currGraph.getOutLabel(), "labelType");
        codeList.add(midCode);
        midCode = new OneAddressCode("LABEL", currGraph.getTopLabel(), "labelType");
        codeList.add(midCode);
    } else_part FI {
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();

        // out label
        OneAddressCode endCode = new OneAddressCode("LABEL", currGraph.getOutLabel(), "labelType");
        codeList.add(endCode);
        currGraph = graphStack.pop();
    };
else_part
    : {
        symbolStack.push(currTable);
        currTable = new Block(currTable);
    } ELSE decl stmt_list {
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();
        // end of else_part, jump to out label
        codeList.add(new OneAddressCode("JUMP", currGraph.getOutLabel(), "labelType"));
    }| /* empty */;
cond              : prevExpr=expr compop postExpr=expr {
    String op1 = $prevExpr.code.getResult();
    String type = $prevExpr.code.getType();
    String op2 = $postExpr.code.getResult();
    String label = currGraph.getClass() == IfGraph.class ? currGraph.getTopLabel() : currGraph.getOutLabel();
    ThreeAddressCode condCode = new ThreeAddressCode(Compop.toIRop($compop.text), op1, op2, label, type);
    codeList.add(condCode);
};
compop            : '<' | '>' | '=' | '!=' | '<=' | '>=';

init_stmt         : assign_expr | /* empty */;
incr_stmt         : assign_expr | /* empty */;


/* ECE 573 students use this version of for_stmt */
for_stmt
    : {
        symbolStack.push(currTable);
        currTable = new Block(currTable);
    } FOR LPAREN init_stmt {
        graphStack.push(currGraph);
        currGraph = new ForGraph();
        OneAddressCode topCode = new OneAddressCode("LABEL", currGraph.getTopLabel(), "labelType");
        codeList.add(topCode);
    } SEMI cond SEMI {
        // before entering incr_stmt, store size of codeList
        currGraph.setStartSize(codeList.size());
    } incr_stmt {
        // after incr_stmt, remove the items after the start size
        // store it for later use
        List<Code> tempList = new ArrayList<>();
        while (codeList.size() > currGraph.getStartSize()) {
            tempList.add(codeList.remove(codeList.size() - 1));
        }
        Collections.reverse(tempList);
        currGraph.setIncrCodeList(tempList);
    } RPAREN decl aug_stmt_list ROF {
       currTable.getParent().addChild(currTable);
       currTable = symbolStack.pop();

       // increment label and append increment_statement
       codeList.add(new OneAddressCode("LABEL", currGraph.getIncrLabel(), "labelType"));
       codeList.addAll(currGraph.getIncrCodeList());
       // jump to start of the loop (top label) then out label
       codeList.add(new OneAddressCode("JUMP", currGraph.getTopLabel(), "labelType"));
       codeList.add(new OneAddressCode("LABEL", currGraph.getOutLabel(), "labelType"));
       currGraph = graphStack.pop();
    };

/* CONTINUE and BREAK statements. ECE 573 students only */
aug_stmt_list     : aug_stmt aug_stmt_list | /* empty */;
aug_stmt          : base_stmt | aug_if_stmt | for_stmt | CONTINUE SEMI {
    // continue, jump to innermost for_loop increment label
    Graph tempGraph = graphStack.lastIndexOfClass(ForGraph.class);
    codeList.add(new OneAddressCode("JUMP", tempGraph.getIncrLabel(), "labelType"));
} | BREAK SEMI {
    // break, jump to innermost for_loop out label
    Graph tempGraph = graphStack.lastIndexOfClass(ForGraph.class);
    codeList.add(new OneAddressCode("JUMP", tempGraph.getOutLabel(), "labelType"));
};

/* Augmented IF statements for ECE 573 students */
aug_if_stmt
    : {
        symbolStack.push(currTable);
        currTable = new Block(currTable);

        graphStack.push(currGraph);
        currGraph = new IfGraph();
    } IF LPAREN cond RPAREN decl aug_stmt_list {
        OneAddressCode midCode = new OneAddressCode("JUMP", currGraph.getOutLabel(), "labelType");
        codeList.add(midCode);
        midCode = new OneAddressCode("LABEL", currGraph.getTopLabel(), "labelType");
        codeList.add(midCode);
    } aug_else_part FI {
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();

        // out label
        OneAddressCode endCode = new OneAddressCode("LABEL", currGraph.getOutLabel(), "labelType");
        codeList.add(endCode);
        currGraph = graphStack.pop();
    };
aug_else_part
    : {
         symbolStack.push(currTable);
         currTable = new Block(currTable);
    } ELSE decl aug_stmt_list {
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();
        // end of else_part, jump to out label
        codeList.add(new OneAddressCode("JUMP", currGraph.getOutLabel(), "labelType"));
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

