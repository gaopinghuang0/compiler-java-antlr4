import java.util.*;

/**
 * Created by hgp on 10/16/2015.
 */
public abstract class Code {
    String opcode;
    String result;
    String type;
    Set<Code> predecessor = new HashSet<>();
    Set<Code> successor = new HashSet<>();
    Set<String> gen = new HashSet<>();
    Set<String> kill = new HashSet<>();
    Set<String> in = new HashSet<>();
    Set<String> out = new HashSet<>();

    abstract String toIR();
    abstract String getOp1();
    abstract String getOp2();

    public String getOpcode() {
        return opcode;
    }
    public String getResult() {
        return result;
    }
    public String getType() {
        return type;
    }

    public Set<Code> getPredecessor() {
        return predecessor;
    }

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
}
