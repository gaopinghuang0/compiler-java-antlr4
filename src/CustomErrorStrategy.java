import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;

/**
 * Created by hgp on 9/15/2015.
 */
public class CustomErrorStrategy extends DefaultErrorStrategy {
    public void reportError(Parser recognizer, RecognitionException e) {
        if(!this.inErrorRecoveryMode(recognizer)) {
            CheckValid cv = new CheckValid();
            cv.setErrorToken();
        }
    }
}
