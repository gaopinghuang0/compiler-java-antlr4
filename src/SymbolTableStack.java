import java.util.ArrayList;

/**
 * Created by hgp on 10/2/2015.
 */
public class SymbolTableStack {
    private ArrayList<SymbolTable> stack = new ArrayList<>();

    public SymbolTable getCurrent() {
        if (stack.isEmpty()) return null;
        return stack.get(stack.size() - 1);
    }

    public void push(SymbolTable table) {
        stack.add(table);
    }

    public SymbolTable pop() {
        if (stack.isEmpty()) return null;
        int last = stack.size() - 1;
        SymbolTable temp = stack.get(last);
        stack.remove(last);
        return temp;
    }
}
