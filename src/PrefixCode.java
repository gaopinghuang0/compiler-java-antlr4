
public class PrefixCode implements Code{
    private String op;
    private String id;
    public PrefixCode(String op, String id)
    {
        this.op = op;
        this.id = id;
    }

    @Override
    public String toIR() {
        return op + " " + id;
    }

    @Override
    public String getOpcode() {
        return null;
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
        return null;
    }

    @Override
    public String getOp2() {
        return null;
    }
}