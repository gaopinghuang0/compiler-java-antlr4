/**
 * Created by hgp on 10/16/2015.
 */
public class TwoAddressCode implements Code {
    private String opcode;
    private String op1;
    private String result;

    public TwoAddressCode(String opcode, String op1, String result) {
        this.opcode = opcode;
        this.op1 = op1;
        this.result = result;
    }

    @Override
    public String toIR() {
        return opcode + " " + op1 + " " + result;
    }

    @Override
    public String toTinyCode() {
        return null;
    }
}
