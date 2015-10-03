import java.util.ArrayList;

/**
 * Created by hgp on 10/2/2015.
 */
public class Block implements SymbolTable {
    private String scope="";
    private SymbolTable parent = null;
    private ArrayList<SymbolEntry> decls = new ArrayList<>();
    private ArrayList<SymbolTable> children = new ArrayList<>();
    private static int count = 0;

    public Block(SymbolTable parent) {
        this.parent = parent;
        this.scope = "BLOCK " + (++count);
//        System.out.println(this.scope);
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
        if (decls.contains(e)) {
            System.out.println("DECLARATION ERROR "+ e.getName());
            return;
        } else {
            for (SymbolTable table = getParent(); table != null; table = table.getParent()) {
                if (table.getDecls().contains(e)) {
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
