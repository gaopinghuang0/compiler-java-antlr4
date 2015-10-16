/**
 * Created by hgp on 10/16/2015.
 */
public class OneAddressCode implements Code{
    private String opcode;
    private String op1;

    public OneAddressCode(String opcode, String op1) {
        this.opcode = opcode;
        this.op1 = op1;
    }

    @Override
    public String toIR() {
        return opcode + " " + op1;
    }

    @Override
    public String toTinyCode() {
        return null;
    }
}
