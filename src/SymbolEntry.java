/**
 * Created by hgp on 10/2/2015.
 */
public class SymbolEntry {
    private String name = "";
    private String type = "";
    private String value = "";
    private String variable = "";

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getVariable() {
        return variable;
    }

    public SymbolEntry(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public SymbolEntry(String name, String type, String value) {
        this(name, type);
        this.value = value;
    }

    public SymbolEntry(String name, String type, String value, String variable) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.variable = variable;
    }

    public final String toString() {
        String out = "name " + name + " type " + type;
        if (this.type.equals("STRING")) {
            out += " value " + value;
        }
        return out;
    }
}
