
/**
 * Created by hgp on 10/16/2015.
 */
public class ThreeAddressCode implements Code {
    private String opcode;
    private String op1;
    private String op2;
    private String result;

    public ThreeAddressCode(String opcode, String op1, String op2, String result) {
        this.opcode = opcode;
        this.op1 = op1;
        this.op2 = op2;
        this.result = result;
    }

    public String toIR() {
        return opcode + " " + op1 + " " + op2 + " " + result;
    }

    @Override
    public String toTinyCode() {
        return null;
    }
}
