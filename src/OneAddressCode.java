/**
 * Created by hgp on 10/16/2015.
 */
public class OneAddressCode extends Node implements Code {
    private String opcode;
    private String result;
    private String type;
    private String label;

    public OneAddressCode(String result, String type) {
        this.result = result;
        this.opcode = null;
        this.type = type;
        this.label = "label" + labelId();
    }

    public OneAddressCode(String opcode, String result, String type) {
        this.opcode = opcode;
        this.result = result;
        this.type = type;
    }
    public String getOp1(){return null;}
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
    public String getLabel() {return label;}
    @Override
    public String toIR() {
        return opcode + " " + result;
    }
}
