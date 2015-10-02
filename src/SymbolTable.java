import java.util.ArrayList;

/**
 * Created by hgp on 10/2/2015.
 */
public class SymbolTable {
    private String table;
    private ArrayList<SymbolEntry> entries = new ArrayList<>();
    private String parent;
    private SymbolTable children;
    private ArrayList<SymbolTable> siblings;

    public SymbolTable(String table) {
        this.table = table;
    }

    public boolean addEntry(SymbolEntry entry) {
        if (this.entries.contains(entry)) {
            return false;
        } else {
            this.entries.add(entry);
            return true;
        }
    }

}
