import java.util.ArrayList;

/**
 * Created by hgp on 10/2/2015.
 */
public class SymbolTable {
    private String scope= "";
    private ArrayList<SymbolEntry> decls = new ArrayList<>();
    private ArrayList<Scope> children = new ArrayList<>();

    public ArrayList<SymbolEntry> getDecls() {
        return decls;
    }

    public ArrayList<Scope> getChildren() {
        return children;
    }

    public void addElement(SymbolEntry e) {
        this.decls.add(e);
    }

    public void addChild(Scope func) {
        this.children.add(func);
    }
    public void printTable() {
        System.out.println("Symbol table " + scope);
        for (SymbolEntry se : this.decls) {
            System.out.println(se);
        }
    }
}
