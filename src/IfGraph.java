import java.util.ArrayList;

/**
 * Created by hgp on 11/1/2015.
 */
public class IfGraph extends Graph {
    private String topLabel = "";
    private String outLabel = "";

    public IfGraph() {
        this.topLabel = nextLable();
        this.outLabel = nextLable();
    }

    @Override
    public String getTopLabel() {
        return topLabel;
    }

    @Override
    public String getOutLabel() {
        return outLabel;
    }

    @Override
    public String getIncrLabel() {
        return null;
    }

    @Override
    public ArrayList<Code> getIncrCodeList() {
        return null;
    }

    @Override
    void setIncrCodeList(ArrayList<Code> list) {
        ;
    }
}
