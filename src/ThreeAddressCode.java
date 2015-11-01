
/**
 * Created by hgp on 10/16/2015.
 */
public class ThreeAddressCode extends Node implements Code {
    private String opcode;
    private String op1;
    private String op2;
    private String result;
    private String type;
    private String label;

    public ThreeAddressCode(String opcode, String op1, String op2, String type) {
        this.opcode = opcode;
        this.op1 = op1;
        this.op2 = op2;
        this.type = type;
    }
    public String getOp1(){return op1;}
    public String getOp2(){return op2;}
    public String getOpcode() {
        return opcode;
    }

    public String getResult() {
        return result;
    }

    public String getType() {
        return type;
    }
    public String getLabel() {return label;}
    public String toIR() {
        return opcode + " " + op1 + " " + op2 + " " + result;
    }

}
