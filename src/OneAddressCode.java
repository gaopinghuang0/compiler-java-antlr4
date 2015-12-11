import java.util.*;

/**
 * Created by hgp on 10/16/2015.
 */
public class OneAddressCode extends Code {
    // keep it, since primary id in Micro.g4 is using
    public OneAddressCode(String result, String type) {
        this(null, result, type);
    }

    public OneAddressCode(String opcode, String result, String type) {
        this.opcode = opcode;
        this.result = result;
        this.type = type;
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

    public List<String> getOpArray() {
        return new ArrayList<>();
    }
}
