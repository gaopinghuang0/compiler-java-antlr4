import java.util.ArrayList;

/**
 * Created by wu on 10/17/2015.
 */
public class Register {
    private int registerCount;
    private ArrayList valueList = new ArrayList();

    public Register(int register_count, String dollar){
        this.registerCount = register_count;
        this.valueList.add(dollar);
    }

    public void add(String dollar){
        this.valueList.add(dollar);
    }
    public String toStirng(){
        return "r" + Integer.toString(registerCount);
    }
    public ArrayList getlist(){
        return this.valueList;
    }
}
