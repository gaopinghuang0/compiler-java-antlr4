/**
 * Created by hgp on 10/16/2015.
 */
public class TwoAddressCode extends Node implements Code {
    private String opcode;
    private String op1;
    private String result;
    private String type;

    // new result id
    public TwoAddressCode(String opcode, String op1, String type) {
        this.opcode = opcode;
        this.op1 = op1;
        this.result = "$T" + nextId();
        this.type = type;
    }

    // existing result id
    public TwoAddressCode(String opcode, String op1, String result, String type) {
        this.opcode = opcode;
        this.op1 = op1;
        this.result = result;
        this.type = type;
    }
    public String getOp1(){return op1;}
    public String getOp2(){return null;}

    public String getOpcode() {
        return opcode;
    }

    public String getResult() {
        return result;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toIR() {
        return opcode + " " + op1 + " " + result;
    }

}
