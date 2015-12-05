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

    public List<SymbolEntry> getCallExprList() {
        return callExprList;
    }

    @Override
    public SymbolTable getParent() {
        return this.parent;
    }

    public void addCallExprEntry(SymbolEntry entry) {
        this.callExprList.add(entry);
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
            System.out.println("DECLARATION ERROR " + e.getName());
            System.exit(1);
        } else {
            for (SymbolTable table = getParent(); table != null; table = table.getParent()) {
                if (isContained(table.getDecls(), e)) {
                    System.out.println("SHADOW WARNING " + e.getName());
                }
            }
        }
        this.getDecls().add(e);
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

    public void printIR(){
        this.printCodeList();
    }
    public void printTable() {
        this.printDecl();
    }

    public void printTiny(int paramId, int localTemp) {
        if (this.getCodeList().size() > 0) {
            TinyCode tc = new TinyCode(this.getCodeList(), paramId, localTemp);
            tc.toTinyCode();
        }
    }

    public void printCodeList(){
        // System.out.println(this.codeList)
        for (Code c : this.getCodeList()) {
                System.out.println(";"+c.toIR());
        }
    }
    public void printDecl() {
        // System.out.println("Symbol table "+scope);
        for (SymbolEntry se : this.getDecls()) {
            if (se.getType().equals("STRING")) {
                System.out.println("str " + se.getName() + " " + se.getVariable() +" " + se.getValue());
            } else {
                //System.out.println("var " + " " + se.getVariable() + " " + se.getName());
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
        return "$P"+paramId++;
    }

    public String getNextDeclId() {
        return "$L" + declId++;
    }

    public String getNextLocalTemp() {
        return "$T" + localTemp++;
    }


    @Override
    public void addParamEntry(String name, String type) {
        String variable = this.getNextParamId();
        SymbolEntry se = new SymbolEntry(name, type, "", variable);
        this.addElement(se);
    }

    @Override
    public void addDeclEntry(String name, String type) {
        String variable = this.getNextDeclId();
        SymbolEntry se = new SymbolEntry(name, type, "", variable);
        this.addElement(se);
    }

    @Override
    public void updateLiveness() {
        this.transferToLinkedList();
        this.buildCFG();
        this.updateInOut();
    }

    public void transferToLinkedList() {
        List<Code> newList = new LinkedList<>(this.codeList);
        this.setCodeList(newList);
    }

    public void buildCFG() {

    }

    public void updateInOut() {

    }
}
