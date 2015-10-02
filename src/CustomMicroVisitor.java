import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Created by hgp on 10/1/2015.
 */
public class CustomMicroVisitor<T> extends MicroBaseVisitor<T>{
    private int count = 0;
//    @Override public T visitProgram(MicroParser.ProgramContext ctx) {
//        return visitChildren(ctx);
//    }

    @Override public T visitPgm_body(MicroParser.Pgm_bodyContext ctx) {
        System.out.println("Symbol table GLOBAL");
//        MicroParser.DeclContext x =  ctx.decl();
//        System.out.println(ctx.getParent());
        return visitChildren(ctx);
    }

    @Override public T visitDecl(MicroParser.DeclContext ctx) {
//        System.out.println(ctx.getParent());
//        System.out.println(ctx.toStringTree());
//        System.out.println("I am in decl"+ count++);
//        System.out.println("In decl");
//        System.out.println(ctx.getChild(0));
        visitDeclHelper(ctx);
        return visitChildren(ctx);
    }

    @Override public T visitFunc_decl(MicroParser.Func_declContext ctx) {
        visitFuncHelper(ctx.getChild(2));
        return visitChildren(ctx);
    }

    public void visitFuncHelper(ParseTree tree) {
        System.out.println("Symbol table "+ tree.getChild(0).getText());
//        System.out.println(tree.getChild(0));
//        for (int i = 0; i < tree.getChildCount(); i++) {
//            System.out.println(tree.getChild(i));
//        }
    }

    public void visitDeclHelper(ParseTree tree) {
        if (tree.getChildCount() != 2) {
            return;
        }
        if (tree.getChild(0).getChildCount() == 3) {
            // var_decl
            visitVarDeclHelper(tree.getChild(0));
        } else {
            visitStrDeclHelper(tree.getChild(0));
        }
    }

    public void visitVarDeclHelper(ParseTree tree) {
        String type = tree.getChild(0).getText();
        String name_text = tree.getChild(1).getText();
        String[] names = name_text.split(",");

        for (String name : names) {
            System.out.println("name "+ name + " type "+type);
        }
    }

    public void visitStrDeclHelper(ParseTree tree) {

    }
}
