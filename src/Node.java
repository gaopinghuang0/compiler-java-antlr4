/**
 * Created by hgp on 10/16/2015.
 */
public class Node {

    private static int currId = 0;

    public int nextId() {
        return ++currId;
    }

}
