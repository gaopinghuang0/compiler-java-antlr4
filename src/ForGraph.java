import java.util.ArrayList;

/**
 * Created by hgp on 11/1/2015.
 */
public class ForGraph extends Graph {
    private String topLabel = "";
    private String incrLabel = "";
    private String outLabel = "";
    private ArrayList<Code> incrCodeList;

    public ForGraph() {
        this.topLabel = nextLable();
        this.incrLabel = nextLable();
        this.outLabel = nextLable();
        this.incrCodeList = new ArrayList<>();
    }

    @Override
    public String getTopLabel() {
        return topLabel;
    }

    @Override
    public String getIncrLabel() {
        return incrLabel;
    }

    @Override
    public String getOutLabel() {
        return outLabel;
    }

    @Override
    public ArrayList<Code> getIncrCodeList() {
        return incrCodeList;
    }

    @Override
    public void setIncrCodeList(ArrayList<Code> incrCodeList) {
        this.incrCodeList = incrCodeList;
    }
}
