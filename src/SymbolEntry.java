/**
 * Created by hgp on 10/2/2015.
 */
public class SymbolEntry {
    private String name = "";
    private String type = "";
    private String value = "";

    public String getName() {
        return name;
    }

    public SymbolEntry(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public SymbolEntry(String name, String type, String value) {
        this(name, type);
        this.value = value;
    }

    public final String toString() {
        String out = "name " + name + " type " + type;
        if (this.type.equals("STRING")) {
            out += " value " + value;
        }
        return out;
    }
}
