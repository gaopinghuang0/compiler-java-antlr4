import java.util.*;

/**
 * Created by hgp on 10/16/2015.
 */
public interface Code {
    String toIR();
    String getOpcode();
    String getResult();
    String getType();
    String getOp1();
    String getOp2();

    Set<Code> getPredecessor();
    Set<Code> getSuccessor();
    Set<String> getGen();
    Set<String> getKill();
    Set<String> getIn();
    Set<String> getOut();
}
