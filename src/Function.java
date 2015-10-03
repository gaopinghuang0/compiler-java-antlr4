import java.util.ArrayList;

/**
 * Created by hgp on 10/2/2015.
 */
public class Function extends Program implements SymbolTable {
    private String scope="";
    private SymbolTable parent = null;
    private ArrayList<SymbolEntry> decls = new ArrayList<>();
    private ArrayList<SymbolTable> children = new ArrayList<>();

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

    @Override
    public SymbolTable getParent() {
        return this.parent;
    }

    public void addElement(SymbolEntry e) {
        if (isContained(decls, e)) {
            System.err.println("DECLARATION ERROR " + e.getName());
            System.exit(1);
        } else {
            for (SymbolTable table = getParent(); table != null; table = table.getParent()) {
                if (isContained(table.getDecls(), e)) {
                    System.out.println("SHADOW WARNING " + e.getName());
                }
            }
        }
        this.decls.add(e);
    }

    public void addChild(SymbolTable func) {
        this.children.add(func);
    }

    public void printTable() {
        System.out.println("Symbol table "+scope);
        for (SymbolEntry se : this.decls) {
            System.out.println(se);
        }
    }
}
