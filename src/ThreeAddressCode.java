import java.util.*;

/**
 * Created by hgp on 10/16/2015.
 */
public class ThreeAddressCode implements Code {
    private String opcode;
    private String op1;
    private String op2;
    private String result;
    private String type;

    private Set<Code> predecessor = new HashSet<>();
    private Set<Code> successor = new HashSet<>();
    private Set<String> gen = new HashSet<>();
    private Set<String> kill = new HashSet<>();
    private Set<String> in = new HashSet<>();
    private Set<String> out = new HashSet<>();

    public ThreeAddressCode(String opcode, String op1, String op2, String result, String type) {
        this.opcode = opcode;
        this.op1 = op1;
        this.op2 = op2;
        this.result = result;
        this.type = type;

        this.gen.add(op1);
        this.gen.add(op2);
        this.kill.add(result);
    }

    public String getOp1() {
        return op1;
    }

    public String getOp2() {
        return op2;
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

    @Override
    public Set<Code> getPredecessor() {
        return predecessor;
    }

    @Override
    public Set<Code> getSuccessor() {
        return successor;
    }

    public Set<String> getGen() {
        return gen;
    }

    public Set<String> getKill() {
        return kill;
    }

    public Set<String> getIn() {
        return in;
    }

    public Set<String> getOut() {
        return out;
    }


    public String toIR() {
        return opcode + " " + op1 + " " + op2 + " " + result;
    }

}
