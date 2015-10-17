
/**
 * Created by hgp on 10/16/2015.
 */
public class ThreeAddressCode extends Node implements Code {
    private String opcode;
    private String op1;
    private String op2;
    private String result;
    private String type;

    public ThreeAddressCode(String opcode, String op1, String op2, String type) {
        this.opcode = opcode;
        this.op1 = op1;
        this.op2 = op2;
        this.result = "$T" + nextId();
        this.type = type;
    }

    public String getOpcode() {
        return opcode;
    }

    public String getResult() {
        return result;
    }

    public String getType() {
        return type;
    }

    public String toIR() {
        return opcode + " " + op1 + " " + op2 + " " + result;
    }

}
