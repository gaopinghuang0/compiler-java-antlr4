/**
 * Created by wu on 11/19/2015.
 */
public class GlobalTinyCode {
    public static int regCounter = 4;
    public static String getNextReg() {
        return "r" + (regCounter++);
    }

}
