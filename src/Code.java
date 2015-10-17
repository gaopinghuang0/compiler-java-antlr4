/**
 * Created by hgp on 10/16/2015.
 */
public interface Code {
    String toIR();

    String toTinyCode();

    String getOpcode();
    String getResult();
    String getType();
}
