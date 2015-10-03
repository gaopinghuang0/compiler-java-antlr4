import java.util.ArrayList;

/**
 * Created by hgp on 10/2/2015.
 */
public class Program extends SymbolTable implements Scope {
    private String scope="GLOBAL";
    private Scope parent = null;
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

    @Override
    public Scope getParent() {
        return this.parent;
    }

    public void printTable() {
        System.out.println("Symbol table "+scope);
        for (SymbolEntry se : this.decls) {
            System.out.println(se);
        }
    }
}
