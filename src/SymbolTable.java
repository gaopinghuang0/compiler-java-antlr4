import java.util.ArrayList;

/**
 * Created by hgp on 10/2/2015.
 */
public interface SymbolTable {
    void setScope(String scope);
    ArrayList<SymbolEntry> getDecls();
    ArrayList<SymbolTable> getChildren();
    void addElement(SymbolEntry e);
    void addChild(SymbolTable func);
    void printTable();
    SymbolTable getParent();
}
