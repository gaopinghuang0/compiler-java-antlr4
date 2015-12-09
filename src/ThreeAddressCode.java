import java.util.*;

/**
 * Created by hgp on 10/16/2015.
 */
public class ThreeAddressCode extends Code {
    private String op1;
    private String op2;

    public ThreeAddressCode(String opcode, String op1, String op2, String result, String type) {
        this.opcode = opcode;
        this.op1 = op1;
        this.op2 = op2;
        this.result = result;
        this.type = type;
    }

    public String getOp1() {
        return op1;
    }
    public String getOp2() {
        return op2;
    }
    public String toIR() {
        return opcode + " " + op1 + " " + op2 + " " + result;
    }

    public List<String> getOpArray(){
        List<String> list = new ArrayList<>();
        list.add(this.op1);
        list.add(this.op2);
//        list.add(this.result);
        return list;
    }
}
