import java.util.ArrayList;
import java.util.List;

/**
 * Created by hgp on 10/2/2015.
 */
public class Program implements SymbolTable {
    private String scope="GLOBAL";
    private SymbolTable parent = null;
    private ArrayList<SymbolEntry> decls = new ArrayList<>();
    private ArrayList<SymbolTable> children = new ArrayList<>();
    private int paramId = 1;
    private int declId = 1;
    private int localTemp = 1;
    private List<Code> codeList = new ArrayList<>();
    public ArrayList<SymbolEntry> getDecls() {
        return decls;
    }

    public ArrayList<SymbolTable> getChildren() {
        return children;
    }
    @Override
    public List<Code> getCodeList() {
        return codeList;
    }

    public void addCode(Code c){
        this.getCodeList().add(c);
    }
    public void addFirst(Code c){
        this.getCodeList().add(0,c);

    }
    public void addOneAddressCode(String opcode, String result, String type){
        Code oneAddressCode = new OneAddressCode(opcode, result, type);
        this.getCodeList().add(oneAddressCode);
    }

    public void addOneAddressCode(String opcode, String result, String type, boolean getNext){
        if (getNext) {
            result = this.getNextLocalTemp();
        }
        Code oneAddressCode = new OneAddressCode(opcode, result, type);
        this.getCodeList().add(oneAddressCode);
    }

    public void addTwoAddressCode(String opcode, String op1, String type){
        String local = this.getNextLocalTemp();
        Code localCode = new TwoAddressCode(opcode, op1, local, type);
        this.getCodeList().add(localCode);
    }
    public void addResultAddressCode(String opcode, String op1,String type){
        Code reusltCode = new TwoAddressCode(opcode,op1,"$R",type);
        Code endCode = new OneAddressCode("RET","","");
        this.getCodeList().add(reusltCode);
        this.getCodeList().add(endCode);
    }

    public void addThreeAddressCode(String opcode, String op1, String op2, String type) {
        String local = this.getNextLocalTemp();
        Code localCode = new ThreeAddressCode(opcode, op1, op2, local, type);
        this.getCodeList().add(localCode);
    }

    public void setScope(String Scope){ this.scope = scope; };

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

    @Override
    public SymbolTable getParent() {
        return this.parent;
    }

    public void printTable() {

        //this.printDecl();
        this.printCodeList();
    }

    public void printTiny(){
        this.printTiny();
    }

    public void printCodeList(){
//        System.out.println(this.codeList)
        for (Code c : this.getCodeList()) {
                System.out.println(";"+c.toIR());
        }
    }
    public void printDecl() {
        //        System.out.println("Symbol table "+scope);
        for (SymbolEntry se : this.getDecls()) {
            if (se.getType().equals("STRING")) {
                System.out.println("str " + se.getName() + " " + se.getVariable() +" " + se.getValue());
            } else {
                System.out.println("var " + " " + se.getVariable() + " " + se.getName());
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
        System.out.println("Warning, cannot find its type: "+name);
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
        System.out.println("Warning, cannot find its type: "+name);
        return name;
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


}
