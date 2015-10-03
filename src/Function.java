import java.util.ArrayList;

/**
 * Created by hgp on 10/2/2015.
 */
public class Function extends Program {
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
}
