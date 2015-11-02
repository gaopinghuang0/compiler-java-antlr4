import org.antlr.v4.codegen.model.AddToLabelList;

import java.util.ArrayList;

/**
 * Created by hgp on 11/1/2015.
 */
public abstract class Graph {
    private static int labelId = 0;
    private boolean incr = false;   // true if entering incr_stmt


    public String nextLable() { return "label" + (++labelId);}

    public boolean isIncr() {
        return incr;
    }

    public void setIncr(boolean flag) {
        this.incr = flag;
    }

    abstract String getTopLabel();
    abstract String getOutLabel();
    abstract String getIncrLabel();
    abstract ArrayList<Code> getIncrCodeList();
    abstract void addToIncrList(Code code);
}
