/**
 * Created by hgp on 11/1/2015.
 */
public abstract class Graph {
    private static int labelId = 0;
    public String nextLable() { return "label" + (++labelId);}

    abstract String getTopLabel();
    abstract String getOutLabel();
    abstract String getIncrLabel();
}
