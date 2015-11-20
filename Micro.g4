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
}

/* Program */
program  returns [ReturnData data]
    : PROGRAM id BEGIN pgm_body END {
        ReturnData data = new ReturnData();
        data.setTable($pgm_body.table);
        data.setCodeList(codeList);
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
        OneAddressCode code1 = new OneAddressCode("LABEL", $id.text, type);
        OneAddressCode code2 = new OneAddressCode("LINK", "", type);
        currTable.addCode(code1);
        currTable.addCode(code2);
    }
    LPAREN param_decl_list RPAREN BEGIN func_body END {
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
        List a = currTable.getCodeList();
        Code c = (Code)a.get(a.size()-1);
        Code code = new TwoAddressCode(opcode, c.getResult(), var, type);
        codeList.add(code);
        // non-auto-increment, use addCode
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
           Code c = new OneAddressCode(op, name, type);
           codeList.add(c);
           //System.out.println(currTable.lookUpVar(name));
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
           Code c = new OneAddressCode(op, name, type);
           codeList.add(c);
           currTable.addOneAddressCode(op, name, type);
       }
    };
    return_stmt       : RETURN expr SEMI{
        //System.out.println($expr.code.getResult());
        //System.out.println($expr.code.getType());
        String type = $expr.code.getType();
        String op = "";
        String result = $expr.code.getResult();
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
            $code = new ThreeAddressCode(op, e.getResult(), f.getResult(), type);
            codeList.add($code);
            // auto-increment, use addThree...
            currTable.addThreeAddressCode(op, e.getResult(), f.getResult(), type);
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
            currTable.addThreeAddressCode(op, f.getResult(), p.getResult(), type);
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
            currTable.addThreeAddressCode($fp.code.getOpcode(), $fp.code.getResult(),$postfix_expr.code.getResult(), type);
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
        // mock a code for global codeList, no real use
        $code = new OneAddressCode("RETURN", "$"+"XX", "INT");
    };
call_expr : id {
        // set base address with the current codeList size
        int size = currTable.getCodeList().size();
        System.out.println("current scope: "+ currTable.getScope());
        System.out.println("base address: " + size);
        currTable.addOffset(size);
} LPAREN expr_list RPAREN {
        currTable.addOneAddressCode("PUSH","","");
        List<Integer> localList = currTable.getOffsetList();
        for (int offset: localList) {
            // TODO: use offset - base address
            System.out.println("offset: "+offset);
            if (offset > 1) {
            String result = currTable.getCodeList().get(offset-1).getResult();
            System.out.println("result: "+result);
            }
        }
        String[] names = $expr_list.text.split(",");
        String type;
        System.out.println("current scope: "+ currTable.getScope());
        for (String name : names) {
            type = currTable.lookUpType(name);
            String lookUpName = currTable.lookUpVar(name);
            currTable.addOneAddressCode("PUSH", lookUpName, type);
        }
        currTable.addOneAddressCode("JSR", $id.text, "LABEL");
        for (String name: names) {
            type = currTable.lookUpType(name);
            currTable.addOneAddressCode("POP", "", type);
        }
        currTable.addOneAddressCode("POP","","", true);
};
expr_list : expr {
    // set first offset of call expr
    int size = currTable.getCodeList().size();
    currTable.addOffset(size);
} expr_list_tail | /* empty */;
expr_list_tail : COMMA expr {
    // set offset
    int size = currTable.getCodeList().size();
    currTable.addOffset(size);
} expr_list_tail | /* empty */;

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

        $code = new TwoAddressCode("STOREI", $INTLITERAL.text, "INT");
        codeList.add($code);
        // auto-increment
        currTable.addTwoAddressCode("STOREI", $INTLITERAL.text, "INT");
    }
    | FLOATLITERAL {
        $code = new TwoAddressCode("STOREF", $FLOATLITERAL.text, "FLOAT");
        codeList.add($code);
        currTable.addTwoAddressCode("STOREF", $FLOATLITERAL.text, "FLOAT");
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
        currTable.addCode(midCode);
        midCode = new OneAddressCode("LABEL", currGraph.getTopLabel(), "labelType");
        codeList.add(midCode);
        currTable.addCode(midCode);
    } else_part FI {
        // out label
        OneAddressCode endCode = new OneAddressCode("LABEL", currGraph.getOutLabel(), "labelType");
        codeList.add(endCode);
        currTable.addCode(endCode);

        // important: add currTable (block-level) codeList to parent's codeList, so that eventually
        // function-level codeList will contain complete code in the right order,
        // so when print codeList, do not print block-level codeList, just function-level
        currTable.getParent().getCodeList().addAll(currTable.getCodeList());

        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();
        currGraph = graphStack.pop();
    };
else_part
    : {
        symbolStack.push(currTable);
        currTable = new Block(currTable);
    } ELSE decl stmt_list {
        // end of else_part, jump to out label
        OneAddressCode elseCode = new OneAddressCode("JUMP", currGraph.getOutLabel(), "labelType");
        codeList.add(elseCode);
        currTable.addCode(elseCode);

        // important: add currTable (block-level) codeList to parent's codeList
        currTable.getParent().getCodeList().addAll(currTable.getCodeList());
        currTable.getParent().addChild(currTable);
        currTable = symbolStack.pop();
    }| /* empty */;
cond              : prevExpr=expr compop postExpr=expr {
    String op1 = $prevExpr.code.getResult();
    String type = $prevExpr.code.getType();
    String op2 = $postExpr.code.getResult();
    String label = currGraph.getClass() == IfGraph.class ? currGraph.getTopLabel() : currGraph.getOutLabel();
    ThreeAddressCode condCode = new ThreeAddressCode(Compop.toIRop($compop.text), op1, op2, label, type);
    codeList.add(condCode);
    // just add it with label already generated
    currTable.addCode(condCode);
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
       currTable.getParent().addChild(currTable);
       currTable = symbolStack.pop();

       // increment label and append increment_statement
       codeList.add(new OneAddressCode("LABEL", currGraph.getIncrLabel(), "labelType"));
       codeList.addAll(currGraph.getIncrCodeList());
       currTable.getCodeList().addAll(currGraph.getIncrCodeList());
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

