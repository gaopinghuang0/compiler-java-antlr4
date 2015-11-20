
public class PrefixCode implements Code{
    private String Opcode;
    private String op1;
    public PrefixCode(String Opcode, String op1)
    {
        this.Opcode = Opcode;
        this.op1 = op1;
    }

    @Override
    public String toIR() {
        return Opcode + " " + op1;
    }

    @Override
    public String getOpcode() {
        return this.Opcode;
    }

    @Override
    public String getResult() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getOp1() {
        return this.op1;
    }

    @Override
    public String getOp2() {
        return null;
    }
}