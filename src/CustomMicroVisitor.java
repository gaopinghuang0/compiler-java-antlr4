import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

/**
 * Created by hgp on 10/1/2015.
 */
public class CustomMicroVisitor<T> extends MicroBaseVisitor<T>{
    @Override public T visitPgm_body(MicroParser.Pgm_bodyContext ctx) {
        System.out.println("I am here");
        return visitChildren(ctx);
    }

}
