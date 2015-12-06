import java.util.Arrays;

/**
 * Created by hgp on 11/1/2015.
 */
public class Compop {
    private static final String[] originOp = {"<", ">", "=", "!=", "<=", ">="};
    public static final String[] IRop = {"GE", "LE", "NE", "EQ", "GT", "LT"};

    public static String toIRop(String opcode) {
        int ind = Arrays.asList(originOp).indexOf(opcode);
        if (ind != -1) {
            return IRop[ind];
        } else {
            return null;
        }
    }
}
