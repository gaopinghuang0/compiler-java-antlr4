import java.util.ArrayList;

/**
 * Created by hgp on 10/2/2015.
 */
public class SymbolStack {
    private ArrayList<Scope> stack = new ArrayList<>();

    public Scope getCurrent() {
        if (stack.isEmpty()) return null;
        return stack.get(stack.size() - 1);
    }

    public void push(Scope scope) {
        stack.add(scope);
    }

    public Scope pop() {
        if (stack.isEmpty()) return null;
        int last = stack.size() - 1;
        Scope temp = stack.get(last);
        stack.remove(last);
        return temp;
    }
}
