import java.util.*;

/**
 * Created by hgp on 10/2/2015.
 */
public class Function extends Program implements SymbolTable {
    private String scope="";
    private SymbolTable parent = null;
    private ArrayList<SymbolEntry> decls = new ArrayList<>();
    private ArrayList<SymbolTable> children = new ArrayList<>();
    private int paramId = 1;
    private int declId = 1;
    private int localTemp = 1;
    private List<Code> codeList = new ArrayList<>();

    public String getScope() {
        return scope;
    }

    public Function(SymbolTable parent) {
        this.parent = parent;
    }


    public void setScope(String scope) {
        this.scope = scope;
    }

    public ArrayList<SymbolEntry> getDecls() {
        return decls;
    }

    public ArrayList<SymbolTable> getChildren() {
        return children;
    }

    public List<Code> getCodeList() {
        return codeList;
    }

    @Override
    public SymbolTable getParent() {
        return this.parent;
    }

    public void addChild(SymbolTable func) {
        this.children.add(func);
    }

}
