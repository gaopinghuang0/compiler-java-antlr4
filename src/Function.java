/**
 * Created by hgp on 10/2/2015.
 */
public class Function extends Program implements SymbolTable {
    private SymbolTable parent = null;

    public Function(SymbolTable parent) {
        this.parent = parent;
    }

    @Override
    public SymbolTable getParent() {
        return this.parent;
    }
}
