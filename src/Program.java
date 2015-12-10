import java.util.*;

/**
 * Created by hgp on 10/2/2015.
 */
public class Program implements SymbolTable {
    private String scope = "GLOBAL";
    private SymbolTable parent = null;
    private ArrayList<SymbolEntry> decls = new ArrayList<>();
    private ArrayList<SymbolTable> children = new ArrayList<>();
    private int paramId = 1;
    private int declId = 1;
    private int localTemp = 1;
    private List<Code> codeList = new ArrayList<>();
    private List<SymbolEntry> callExprList = new ArrayList<>();
    public String getScope() {
        return scope;
    }

    public void setScope(String scope){ this.scope = scope; };

    public ArrayList<SymbolEntry> getDecls() {
        return decls;
    }

    public int getParamId(){
        return this.paramId;
    }
    public int getLocalTemp() {return this.localTemp;}
    @Override
    public int getDeclId(){
        return this.declId;
    }

    public ArrayList<SymbolTable> getChildren() {
        return children;
    }

    @Override
    public List<Code> getCodeList() {
        return codeList;
    }

    public void setCodeList(List<Code> codeList) {
        this.codeList = codeList;
    }

    @Override
    public SymbolTable getParent() {
        return this.parent;
    }

    public void addCode(Code c){
        this.codeList.add(c);
    }

    public Code addOneAddressCode(String opcode, String result, String type){
        Code localCode = new OneAddressCode(opcode, result, type);
        this.codeList.add(localCode);
        return localCode;
    }

    public Code addOneAddressCode(String opcode, String result, String type, boolean getNext){
        if (getNext) {
            result = this.getNextLocalTemp();
        }
        Code localCode = new OneAddressCode(opcode, result, type);
        this.codeList.add(localCode);
        return localCode;
    }

    public Code addTwoAddressCode(String opcode, String op1, String type){
        String local = this.getNextLocalTemp();
        Code localCode = new TwoAddressCode(opcode, op1, local, type);
        this.codeList.add(localCode);
        return localCode;
    }

    public Code addResultAddressCode(String opcode, String op1,String type){
        Code reusltCode = new TwoAddressCode(opcode,op1,"$R",type);
        Code endCode = new OneAddressCode("RET","","");
        this.codeList.add(reusltCode);
        this.codeList.add(endCode);
        return reusltCode;
    }

    public Code addThreeAddressCode(String opcode, String op1, String op2, String type) {
        String local = this.getNextLocalTemp();
        Code localCode = new ThreeAddressCode(opcode, op1, op2, local, type);
        this.codeList.add(localCode);
        return localCode;
    }

    public void addElement(SymbolEntry e) {
        if (isContained(this.getDecls(), e)) {
            System.err.println("DECLARATION ERROR " + e.getName());
            System.exit(1);
        } else {
            for (SymbolTable table = getParent(); table != null; table = table.getParent()) {
                if (isContained(table.getDecls(), e)) {
                    System.err.println("SHADOW WARNING " + e.getName());
                }
            }
        }
        this.decls.add(e);
    }


    public boolean isContained(ArrayList<SymbolEntry> decls, SymbolEntry entry) {
        for (SymbolEntry d : decls) {
            if (d.getName().equals(entry.getName()) && d.getType().equals(entry.getType())) {
                return true;
            }
        }
        return false;
    }

    public void addChild(SymbolTable func) {
        this.children.add(func);
    }

    public void printIR() {
        for (Code c : this.codeList) {
            System.out.println(";"+c.toIR());
        }
    }
    public void printCodeOut(){
        for (Code c: this.codeList){
            System.out.println(c.getOut());
        }
    }
    public void printTable() {
        this.printDecl();
    }

    public void printDecl() {
        // System.out.println("Symbol table "+scope);
        for (SymbolEntry se : this.getDecls()) {
            if (se.getType().equals("STRING")) {
                System.out.println("str " + se.getName() + " " + se.getVariable() +" " + se.getValue());
            } else {
//                System.out.println("var " + " " + se.getVariable() + " " + se.getName());
            }
        }
    }
    public void printTiny(int paramId, int localTemp, int declId) {
        if (codeList.size() > 0) {
            //initRegMap();
            TinyCode tc = new TinyCode(codeList, paramId, localTemp, declId);
            tc.toTinyCode();
        }
    }

    public void printCodeList() {
        for (Code c : codeList) {
            System.out.println(";-------------"+c.toIR());
            System.out.println(";gen: "+c.getGen());
            System.out.println(";kill: "+c.getKill());
            System.out.println(";predecessor: "+c.getPredecessor());
            System.out.println(";successor: "+c.getSuccessor());
            System.out.println(";IN: "+c.getIn());
            System.out.println(";OUT: "+c.getOut());
            for(String s : c.getOut()){
                System.out.println(s);
            }

        }
    }

    @Override
    public String lookUpType(String name) {
        for (SymbolEntry se : this.getDecls()) {
            if (se.getName().equals(name)) {
                return se.getType();
            }
        }
        for (SymbolTable table = getParent(); table != null; table = table.getParent()) {
            for (SymbolEntry se : table.getDecls()) {
                if (se.getName().equals(name)) {
                    return se.getType();
                }
            }
        }
        System.err.println("Warning, cannot find its type: "+name);
        return null;
    }

    @Override
    public String lookUpVar(String name) {
        for (SymbolEntry se : this.getDecls()) {
            if (se.getName().equals(name)) {
                return se.getVariable();
            }
        }
        for (SymbolTable table = getParent(); table != null; table = table.getParent()) {
            for (SymbolEntry se : table.getDecls()) {
                if (se.getName().equals(name)) {
                    return se.getVariable();
                }
            }
        }
        return null;
    }

    public String getNextParamId() {
        return "$P" + paramId++;
    }

    public String getNextDeclId() {
        return "$L" + declId++;
    }

    public String getNextLocalTemp() {
        return "$T" + localTemp++;
    }


    @Override
    public void addParamEntry(String name, String type) {
        String variable = getNextParamId();
        SymbolEntry se = new SymbolEntry(name, type, "", variable);
        addElement(se);
    }

    @Override
    public void addDeclEntry(String name, String type) {
        String variable = getNextDeclId();
        SymbolEntry se = new SymbolEntry(name, type, "", variable);
        addElement(se);
    }

    @Override
    public void doLivenessAnalysis(List<String> globalTemp) {
        initGenKillInOut(globalTemp);
        transferToLinkedList();
        buildCFG();
        buildBasicBlock();
        updateInOut();
//        printCodeList();
    }

    public void initGenKillInOut(List<String> globalTemp) {
        for (Code c : this.codeList) {
            String opcode = c.getOpcode();
            String result = c.getResult();
            if (c.getClass() == OneAddressCode.class) {
                initOneAddressCode(c, opcode, result, globalTemp);
            } else if (c.getClass() == TwoAddressCode.class) {
                // twoAddressCode, add each result into kill and non-literal op1 into gen
                String op1 = c.getOp1();
                if (op1.startsWith("$")) {
                    c.addGen(op1);
                }
                c.addKill(result);
            } else {  // threeAddressCode
                c.addGen(c.getOp1());
                c.addGen(c.getOp2());
                if (!result.startsWith("label")) {
                    c.addKill(result);
                }
            }
        }
    }

    public void initOneAddressCode(Code c, String opcode, String result, List<String> globalTemp) {
        // ignore str variable, ie., ignore WRITES and READS
        if (opcode.equals("PUSH") || opcode.equals("WRITEI") || opcode.equals("WRITEF")) {
            c.addGen(result);
        } else if (opcode.equals("POP") || opcode.startsWith("READI") || opcode.equals("READF")) {
            c.addKill(result);
        } else if (opcode.equals("JSR")) {
            // gen is all global variables and kill is empty
            for (String s : globalTemp) {
                c.addGen(s);
            }
        } else if (opcode.equals("RET")) {
            // OUT of RETURN stmt is init to all global variables
            for (String s : globalTemp) {
                c.addOut(s);
            }
        } else {
            // LABEL, JUMP..., kill and gen are empty
        }
    }

    public void transferToLinkedList() {
        this.codeList = new LinkedList<>(this.codeList);
    }

    public void buildCFG() {
        if (codeList.size() == 0) {
            return;
        }

        // iterate codeList, update predecessor and successor
        int i = 0;
        codeList.get(i).markAsLeader();
        for (; i < codeList.size() - 1; i++) {
            Code curr = codeList.get(i);
            Code next = codeList.get(i + 1);

//            if (curr.getOpcode().equals("RET")) {
//                continue;
//            }

            // no successor for return node, not be a predecessor for next node either
            // normal stmt or conditionally jump fall-through target
            if (!curr.getOpcode().equals("JUMP")) {
                curr.addSuccessor(next);
                next.addPredecessor(curr);
            }

            // explicit target of conditionally jump or unconditionally jump
            if (Arrays.asList(Compop.IRop).contains(curr.getOpcode()) ||
                    curr.getOpcode().equals("JUMP")) {
                String label = curr.getResult();
                Code target = lookUpTargetByLabel(label);
                target.addPredecessor(curr);
                curr.addSuccessor(target);

                // mark implicit and explicit target as leader
                for (Code succ : curr.getSuccessor()) {
                    succ.markAsLeader();
                }
            }
        }

        // last node (return node), no successor
        if (!codeList.get(i).getOpcode().equals("RET")) {
            System.err.println("last node of " + scope + " is not return");
        }
    }

    public void buildBasicBlock() {
        if (codeList.size() == 0) {
            return;
        }

        // iterate codeList, mark tail
        int i = 0;
        for (; i < codeList.size() - 1; i++) {
            Code curr = codeList.get(i);
            Code next = codeList.get(i + 1);

            if (next.isLeader()) {
                curr.markAsTail();
            }
        }
        // last one
        codeList.get(i).markAsTail();
    }

    public Code lookUpTargetByLabel(String label) {
        if (!label.startsWith("label")) {
            System.err.println("look up should be label, but is: " + label);
        }
        for (Code c : codeList) {
            if (c.getOpcode().equals("LABEL") && c.getResult().equals(label)) {
                return c;
            }
        }
        return null;
    }

    public void updateInOut() {
        for (Code c : codeList) {
            _updateInOut(c);
        }
    }

    public void _updateInOut(Code c) {
        if (!c.getOpcode().equals("RET")) {
            // OUT of return node is already init to all global var
            // but calcOut will always return empty set for return node
            c.setOut(calcOut(c));
        }
        Set<String> newIn = calcIn(c);
//        System.out.println("Node: " + c.toIR());
//        System.out.println("newIn: " + newIn + " oldIn: " + c.getIn());
        if (!newIn.equals(c.getIn())) {  // update all predecessor if different
            c.setIn(newIn);
            for (Code pred : c.getPredecessor()) {
                _updateInOut(pred);
            }
        }
    }

    public Set<String> calcOut(Code c) {
        // merge all IN of successor
        Set<String> result = new HashSet<>();
        for (Code succ : c.getSuccessor()) {
            result.addAll(succ.getIn());
        }
        return result;
    }

    public Set<String> calcIn(Code c) {
        Set<String> result = new HashSet<>(c.getOut());
        // remove kill set from OUT
        result.removeAll(c.getKill());
        result.addAll(c.getGen());
        return result;
    }


}
