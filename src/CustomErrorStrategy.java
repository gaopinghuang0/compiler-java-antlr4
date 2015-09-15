import org.antlr.v4.runtime.*;

/**
 * Created by hgp on 9/14/2015.
 */
public class CustomErrorStrategy extends DefaultErrorStrategy {

    public void reset(Parser var1) {

    }

    public Token recoverInline(Parser var1) throws RecognitionException {
        return null;
    }

    public void recover(Parser var1, RecognitionException var2) throws RecognitionException {

    }

    public void sync(Parser var1) throws RecognitionException {

    }

    public boolean inErrorRecoveryMode(Parser var1) {
        return false;
    }

    public void reportMatch(Parser var1) {
//        System.out.println("Accepted");
    }

    public void reportError(Parser var1, RecognitionException var2) {
        System.out.println("Not accepted");
    }
}
