import java.util.*;

/**
 * Created by hgp on 10/16/2015.
 */
public class TwoAddressCode extends Node implements Code {
    private String opcode;
    private String op1;
    private String result;
    private String type;

    private Set<Code> predecessor = new HashSet<>();
    private Set<Code> successor = new HashSet<>();
    private Set<String> gen = new HashSet<>();
    private Set<String> kill = new HashSet<>();
    private Set<String> in = new HashSet<>();
    private Set<String> out = new HashSet<>();

    // obsolete since no global-level $T variable, only function-level $T var
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

        this.gen.add(op1);
        this.kill.add(result);
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

    @Override
    public String toIR() {
        return opcode + " " + op1 + " " + result;
    }

}
