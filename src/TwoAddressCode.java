import java.util.*;

/**
 * Created by hgp on 10/16/2015.
 */
public class TwoAddressCode extends Code {
    private String op1;

    // existing result id
    public TwoAddressCode(String opcode, String op1, String result, String type) {
        this.opcode = opcode;
        this.op1 = op1;
        this.result = result;
        this.type = type;

        this.gen.add(op1);
        this.kill.add(result);
    }

    public String getOp1() {
        return op1;
    }
    public String getOp2() {
        return null;
    }
    public String toIR() {
        return opcode + " " + op1 + " " + result;
    }

}
