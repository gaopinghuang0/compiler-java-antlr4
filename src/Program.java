import java.util.ArrayList;

/**
 * Created by hgp on 10/2/2015.
 */
public class Program implements SymbolTable {
    private String scope="GLOBAL";
    private SymbolTable parent = null;
    private ArrayList<SymbolEntry> decls = new ArrayList<>();
    private ArrayList<SymbolTable> children = new ArrayList<>();

    public ArrayList<SymbolEntry> getDecls() {
        return decls;
    }

    public ArrayList<SymbolTable> getChildren() {
        return children;
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
//        System.out.println("Symbol table "+scope);
        for (SymbolEntry se : this.getDecls()) {
            if (se.getType().equals("STRING")) {
                System.out.println("str " + se.getName() + " " + se.getValue());
            } else {
                System.out.println("var " + se.getName());
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
        System.out.println("Warning, cannot find its type");
        return null;
    }
}
