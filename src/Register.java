import java.util.ArrayList;

/**
 * Created by wu on 10/17/2015.
 */
public class Register {
    private int register_count;
    private ArrayList value_list = new ArrayList();

    public Register(int register_count, String dollar){
        this.register_count = register_count;
        this.value_list.add(dollar);
    }

    public void add(String dollar){
        this.value_list.add(dollar);
    }
    public String toStirng(){
        return "r" + Integer.toString(register_count);
    }
    public ArrayList getlist(){
        return this.value_list;
    }
}
