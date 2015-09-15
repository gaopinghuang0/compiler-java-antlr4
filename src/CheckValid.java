/**
 * Created by hgp on 9/15/2015.
 */
public class CheckValid {
    private static boolean errorToken = false;

    public void setErrorToken() {
        this.errorToken = true;
    }

    public boolean hasError() {
        return this.errorToken;
    }
}
