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
    boolean isLeader = false;
    boolean isTail = false;

    abstract String toIR();
    abstract String getOp1();
    abstract String getOp2();
    abstract List<String> getOpArray();
    public String getOpcode() {
        return opcode;
    }
    public String getResult() {
        return result;
    }
    public String getType() {
        return type;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public boolean isTail() {
        return isTail;
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

    public void addGen(String var) {
        if (var != null && !var.isEmpty()) {
            this.gen.add(var);
        }
    }

    public void addKill(String var) {
        if (var != null && !var.isEmpty()) {
            this.kill.add(var);
        }
    }

    public void addIn(String var) {
        if (var != null && !var.isEmpty()) {
            this.in.add(var);
        }
    }

    public void addOut(String var) {
        if (var != null && !var.isEmpty()) {
            this.out.add(var);
        }
    }

    public void addPredecessor(Code code) {
        this.predecessor.add(code);
    }

    public void addSuccessor(Code code) {
        this.successor.add(code);
    }

    public void setIn(Set<String> in) {
        this.in = in;
    }

    public void setOut(Set<String> out) {
        this.out = out;
    }

    public void markAsLeader() {
        this.isLeader = true;
    }

    public void markAsTail() {
        this.isTail = true;
    }

    public String toString() {
        return toIR();
    }
}
