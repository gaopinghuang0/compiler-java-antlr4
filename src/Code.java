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
}
