import java.util.*;

/**
 * Created by hgp on 10/2/2015.
 */
public class CostomStack<T> {
    private List<T> stack = new ArrayList<>();

    public T getCurrent() {
        if (stack.isEmpty()) return null;
        return stack.get(stack.size() - 1);
    }

    public void push(T table) {
        stack.add(table);
    }

    public T pop() {
        if (stack.isEmpty()) return null;
        int last = stack.size() - 1;
        T temp = stack.get(last);
        stack.remove(last);
        return temp;
    }

    /**
     * Find the latest element in the list who has the same Class
     * For example, it can be used to find the innermost nested for_loop
     * @param c
     * @return
     */
    public T lastIndexOfClass(Class c) {
        int last = stack.size() - 1;
        for (int i = last; i > 0; i--) {
            if (stack.get(i).getClass() == c) {
                return stack.get(i);
            }
        }
        return null;
    }
}
