import java.util.*;

/**
 * Created by hgp on 10/16/2015.
 */
public class OneAddressCode extends Code {
    // keep, since primary id in Micro.g4 is using this
    public OneAddressCode(String result, String type) {
        this.result = result;
        this.opcode = null;
        this.type = type;
    }

    public OneAddressCode(String opcode, String result, String type) {
        this.opcode = opcode;
        this.result = result;
        this.type = type;

        //TODO: handle some special tricky cases
//        this.gen.add(op1);
        this.kill.add(result);
    }
    public String getOp1() {
        return null;
    }
    public String getOp2() {
        return null;
    }
    public String toIR() {
        return opcode + " " + result;
    }
}
