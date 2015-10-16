import java.util.ArrayList;

/**
 * Created by hgp on 10/16/2015.
 */
public class ReturnData {
    private SymbolTable table;
    private ArrayList<Code> codeList = new ArrayList<>();

    public SymbolTable getTable() {
        return table;
    }

    public void setTable(SymbolTable table) {
        this.table = table;
    }

    public ArrayList<Code> getCodeList() {
        return codeList;
    }

    public void setCodeList(ArrayList<Code> codeList) {
        this.codeList = codeList;
    }
}
