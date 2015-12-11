grammar Micro;

@header {
    import java.util.*;
}

@members {
    SymbolTable currTable = null;
    CostomStack<SymbolTable> symbolStack = new CostomStack<>();
    CostomStack<Graph> graphStack = new CostomStack<>();
    Graph currGraph = null;
    List<SymbolEntry> currCallExprList = null;
    CostomStack<List> callExprStack = new CostomStack<>();
}

/* Program */
program  returns [ReturnData data]
    : PROGRAM id BEGIN pgm_body END {
        ReturnData data = new ReturnData();
        data.setTable($pgm_body.table);
        data.setTableStack(symbolStack);
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
            currTable.addDeclEntry(name, $var_type.text);
        }
    };
var_type	      : FLOAT | INT;
any_type          : var_type | VOID;
id_list           : id id_tail;
id_tail           : COMMA id id_tail   | /* empty */;

/* Function Paramater List */
param_decl_list   : param_decl param_decl_tail | /* empty */;
param_decl        : var_type id {
        currTable.addParamEntry($id.text, $var_type.text);
    };
param_decl_tail   : COMMA param_decl param_decl_tail | /* empty */;

/* Function Declarations */
func_declarations : func_decl func_declarations | /* empty */;
func_decl
    : {
        symbolStack.push(currTable);
        currTable = new Function(currTable);
    } FUNCTION any_type id {
        String type = $any_type.text;
        currTable.setScope($id.text);
        OneAddressCode code1 = new OneAddressCode("LABEL", $id.text, type);
        OneAddressCode code2 = new OneAddressCode("LINK", "", type);
        currTable.addCode(code1);
        currTable.addCode(code2);
    }
    LPAREN param_decl_list RPAREN BEGIN func_body END {
        // in case of no return stmt at the end, check the last IR node
        // if not a "RET" instruction, append one at the end
        List cl = currTable.getCodeList();
        Code c = (Code)cl.get(cl.size()-1);
        if (!c.toIR().startsWith("RET")) {
            Code endCode = new OneAddressCode("RET","","");
            currTable.addCode(endCode);
        }


        // at the end of each function, save and reset localDeclId, staticLocalTemp to 1
        // so that both function-level and block-level localDeclId and staticLocalTemp can
        // start increasing from 1
        currTable.saveAndResetId();

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
        String type = currTable.lookUpType($id.text);
        String opcode = "";
        if (type != null && type.equals("INT")) {
            opcode = "STOREI";
        } else if (type != null && type.equals("FLOAT")) {
            opcode = "STOREF";
        } else {
            System.out.println("in assign_expr"+$expr.text);
        }
        String var = currTable.lookUpVar($id.text);
        String result = currTable.lookUpVar($expr.text);

        if (result == null) {
            List cl = currTable.getCodeList();
            Code c = (Code)cl.get(cl.size()-1);
            result = c.getResult();
        }

        // non-auto-increment, use addCode
        Code code = new TwoAddressCode(opcode, result, var, type);
        currTable.addCode(code);
    };
read_stmt
    : READ LPAREN id_list RPAREN SEMI {
       String[] names = $id_list.text.split(",");
       for (String name : names) {
           String type = currTable.lookUpType(name);
           String op;
           if (type.equals("INT")) {
               op = "READI";
           } else if (type.equals("FLOAT")) {
               op = "READF";
           } else {
               op = "READS";
           }
           String lookUpName = currTable.lookUpVar(name);
           currTable.addOneAddressCode(op, lookUpName, type);
       }
    };
write_stmt
    : WRITE LPAREN id_list RPAREN SEMI {
       String[] names = $id_list.text.split(",");
       for (String name : names) {
           String type = currTable.lookUpType(name);
           String op;
           if (type.equals("INT")) {
               op = "WRITEI";
               name = currTable.lookUpVar(name);
           } else if (type.equals("FLOAT")) {
               op = "WRITEF";
               name = currTable.lookUpVar(name);
           } else {
               op = "WRITES";

           }
           currTable.addOneAddressCode(op, name, type);
       }
    };
    return_stmt       : RETURN expr SEMI{
        //System.out.println($expr.code.getResult());
        //System.out.println($expr.code.getType());
        String type = $expr.code.getType();
        String op = "";
        String result = $expr.code.getResult();
        // use the last code in the codeList as the return result
        op = type.equals("INT") ? "STOREI" : "STOREF";
        currTable.addResultAddressCode(op, result, type);

    };

/* Expressions */
expr  returns [Code code]
    : expr_prefix factor {
        Code e = $expr_prefix.code;
        Code f = $factor.code;
        String type = f.getType();
        if (e != null && type != null) {
            String op = e.getOpcode();
            // auto-increment, use addThree...
            $code = currTable.addThreeAddressCode(op, e.getResult(), f.getResult(), type);
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
        if ($ep.code != null) {  // complex expression
            //TODO: re-check this later: whether need to append to local codeList?
            $code = currTable.addThreeAddressCode($ep.code.getOpcode(), $ep.code.getResult(),
                    $factor.code.getResult(), type);
            $code = new OneAddressCode(op, $code.getResult(), type);
        } else {
            // TODO: re-check this later: whether need to append to local codeList?
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
            $code = currTable.addThreeAddressCode(op, f.getResult(), p.getResult(), type);
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
            //TODO: re-check this later: whether need to append to local codeList?
            $code = currTable.addThreeAddressCode($fp.code.getOpcode(), $fp.code.getResult(),$postfix_expr.code.getResult(), type);
            $code = new OneAddressCode(op, $code.getResult(), type);
        } else {
            //TODO: re-check this later: whether need to append to local codeList?
            $code = new OneAddressCode(op, $postfix_expr.code.getResult(), type);
        }
    }| /* empty */;
postfix_expr  returns [Code code]
    : primary {
        $code = $primary.code;
    }
    | call_expr {
        // mock a code for global codeList, no real use
        //  $code = new OneAddressCode("RETURN", "$"+"XX", "INT");

        // use the last code in codeList
        int size = currTable.getCodeList().size();
        $code = currTable.getCodeList().get(size-1);
    };
call_expr : id LPAREN {
    // use a stack to handle nested call expr list
    callExprStack.push(currCallExprList);
    currCallExprList = new ArrayList<>();
} expr_list RPAREN {
    currTable.addOneAddressCode("PUSH","","");
    for (SymbolEntry se : currCallExprList) {
        currTable.addOneAddressCode("PUSH", se.getName(), se.getType());
    }
    currTable.addOneAddressCode("JSR", $id.text, "LABEL");
    for (SymbolEntry se : currCallExprList) {
        currTable.addOneAddressCode("POP", "", se.getType());
    }
    currCallExprList = callExprStack.pop();
    currTable.addOneAddressCode("POP","","", true);
};
expr_list : expr {
    // add callExpr entry based on whether primary id or expression
    String name = currTable.lookUpVar($expr.text);
    SymbolEntry se;
    if (name != null) {  // primary id
        String type = currTable.lookUpType($expr.text);
        se = new SymbolEntry(name, type);
    } else {  // expression
        int size = currTable.getCodeList().size();
        Code c = currTable.getCodeList().get(size-1);
        se = new SymbolEntry(c.getResult(), c.getType());
    }
    currCallExprList.add(se);
} expr_list_tail | /* empty */;
expr_list_tail : COMMA expr expr_list_tail
{
    // add callExpr entry based on whether primary id or expression
    String name = currTable.lookUpVar($expr.text);
    SymbolEntry se;
    if (name != null) {  // primary id
        String type = currTable.lookUpType($expr.text);
        se = new SymbolEntry(name, type);
    } else {  // expression
        int size = currTable.getCodeList().size();
        Code c = currTable.getCodeList().get(size-1);
        se = new SymbolEntry(c.getResult(), c.getType());
    }
    currCallExprList.add(se);
} | /* empty */;

primary  returns [Code code]
    : LPAREN expr RPAREN {
        $code = $expr.code;
    }
    | id {
        // look up type from currTable to it's parent
        String type = currTable.lookUpType($id.text);
        String var = currTable.lookUpVar($id.text);
        $code = new OneAddressCode(var, type);
    }
    | INTLITERAL {
        // auto-increment temporary counter
        $code = currTable.addTwoAddressCode("STOREI", $INTLITERAL.text, "INT");
    }
    | FLOATLITERAL {
        $code = currTable.addTwoAddressCode("STOREF", $FLOATLITERAL.text, "FLOAT");
    };
addop             : '+' | '-';
mulop             : '*' | '/';

/* Complex Statements and Condition */
if_stmt
    : {
        symbolStack.push(currTable);
        currTable = new Block(currTable);
        currTable.setScope("BLOCK");

        graphStack.push(currGraph);
        currGraph = new IfGraph();
    } IF LPAREN cond RPAREN decl stmt_list {
        OneAddressCode midCode = new OneAddressCode("JUMP", currGraph.getOutLabel(), "labelType");
        currTable.addCode(midCode);
        midCode = new OneAddressCode("LABEL", currGraph.getTopLabel(), "labelType");
        currTable.addCode(midCode);
    } else_part FI {
        // out label
        OneAddressCode endCode = new OneAddressCode("LABEL", currGraph.getOutLabel(), "labelType");
        currTable.addCode(endCode);
        currGraph = graphStack.pop();

        // important, append all if-block-level codes to its parent codeList
        currTable.getParent().appendToCodeList(currTable.getCodeList());
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();
    };
else_part
    : ELSE decl stmt_list {
        // end of else_part, jump to out label
        OneAddressCode elseCode = new OneAddressCode("JUMP", currGraph.getOutLabel(), "labelType");
        currTable.addCode(elseCode);
    }| /* empty */;
cond              : prevExpr=expr compop postExpr=expr {
    String op1 = $prevExpr.code.getResult();
    String type = $prevExpr.code.getType();
    String op2 = $postExpr.code.getResult();
    String label = currGraph.getClass() == IfGraph.class ? currGraph.getTopLabel() : currGraph.getOutLabel();
    ThreeAddressCode condCode = new ThreeAddressCode(Compop.toIRop($compop.text), op1, op2, label, type);
    // just add it with label already generated
    currTable.addCode(condCode);
};
compop            : '<' | '>' | '=' | '!=' | '<=' | '>=';

init_stmt         : assign_expr | /* empty */;
incr_stmt         : assign_expr | /* empty */;


/* ECE 573 students use this version of for_stmt */
for_stmt
    : FOR {
        symbolStack.push(currTable);
        currTable = new Block(currTable);
        currTable.setScope("BLOCK");
    } LPAREN init_stmt {
        graphStack.push(currGraph);
        currGraph = new ForGraph();
        OneAddressCode topCode = new OneAddressCode("LABEL", currGraph.getTopLabel(), "labelType");
        currTable.addCode(topCode);
    } SEMI cond SEMI {
        // before entering incr_stmt, store size of codeList
        currGraph.setStartSize(currTable.getCodeList().size());
    } incr_stmt {
        // after incr_stmt, remove the items after the start size
        // store it for later use
        ArrayList<Code> tempList = new ArrayList<>();
        List currCodeList = currTable.getCodeList();
        while (currCodeList.size() > currGraph.getStartSize()) {
            tempList.add((Code)currCodeList.remove(currCodeList.size() - 1));
        }
        Collections.reverse(tempList);
        currGraph.setIncrCodeList(tempList);
    } RPAREN decl aug_stmt_list ROF {
       // increment label and append increment_statement
       currTable.addCode(new OneAddressCode("LABEL", currGraph.getIncrLabel(), "labelType"));
       currTable.getCodeList().addAll(currGraph.getIncrCodeList());

       // jump to start of the loop (top label) then out label
       currTable.addCode(new OneAddressCode("JUMP", currGraph.getTopLabel(), "labelType"));
       currTable.addCode(new OneAddressCode("LABEL", currGraph.getOutLabel(), "labelType"));
       currGraph = graphStack.pop();

       // important, append all for-block-level codes to its parent codeList
       currTable.getParent().appendToCodeList(currTable.getCodeList());
       currTable.getParent().addChild(currTable);
       currTable = symbolStack.pop();
    };

/* CONTINUE and BREAK statements. ECE 573 students only */
aug_stmt_list     : aug_stmt aug_stmt_list | /* empty */;
aug_stmt          : base_stmt | aug_if_stmt | for_stmt | CONTINUE SEMI {
    // continue, jump to innermost for_loop increment label
    Graph tempGraph = graphStack.lastIndexOfClass(ForGraph.class);
    currTable.addCode(new OneAddressCode("JUMP", tempGraph.getIncrLabel(), "labelType"));
} | BREAK SEMI {
    // break, jump to innermost for_loop out label
    Graph tempGraph = graphStack.lastIndexOfClass(ForGraph.class);
    currTable.addCode(new OneAddressCode("JUMP", tempGraph.getOutLabel(), "labelType"));
};

/* Augmented IF statements for ECE 573 students */
aug_if_stmt
    : {
        symbolStack.push(currTable);
        currTable = new Block(currTable);
        currTable.setScope("BLOCK");

        graphStack.push(currGraph);
        currGraph = new IfGraph();
    } IF LPAREN cond RPAREN decl aug_stmt_list {
        OneAddressCode midCode = new OneAddressCode("JUMP", currGraph.getOutLabel(), "labelType");
        currTable.addCode(midCode);

        midCode = new OneAddressCode("LABEL", currGraph.getTopLabel(), "labelType");
        currTable.addCode(midCode);
    } aug_else_part FI {
        // out label
        OneAddressCode endCode = new OneAddressCode("LABEL", currGraph.getOutLabel(), "labelType");
        currTable.addCode(endCode);
        currGraph = graphStack.pop();

        // important, append all for-block-level codes to its parent codeList
        currTable.getParent().appendToCodeList(currTable.getCodeList());
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();
    };
aug_else_part
    : ELSE {
         symbolStack.push(currTable);
         currTable = new Block(currTable);
         currTable.setScope("BLOCK");
    }  decl aug_stmt_list {
        // end of else_part, jump to out label
        currTable.addCode(new OneAddressCode("JUMP", currGraph.getOutLabel(), "labelType"));

        // important, append all for-block-level codes to its parent codeList
        currTable.getParent().appendToCodeList(currTable.getCodeList());
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
FLOATLITERAL: [0-9]+'.'[0-9]+;
STRINGLITERAL:'"'~["]*'"';
COMMENT: '--'~[\r\n]* -> skip;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

