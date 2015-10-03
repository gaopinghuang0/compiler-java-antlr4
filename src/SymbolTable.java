import java.util.ArrayList;

/**
 * Created by hgp on 10/2/2015.
 */
public interface SymbolTable {
    public ArrayList<SymbolEntry> getDecls();
    public ArrayList<SymbolTable> getChildren();
    public void addElement(SymbolEntry e);
    public void addChild(SymbolTable func);
    public void printTable();
    SymbolTable getParent();
}
