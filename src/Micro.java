import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;


public class Micro {
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            String filename = args[i];
            ANTLRFileStream input = new ANTLRFileStream(filename);
            MicroLexer lexer = new MicroLexer(input);
            MicroParser parser = new MicroParser(new CommonTokenStream(lexer));
            ANTLRErrorStrategy es = new CustomErrorStrategy();
            parser.setErrorHandler(es);
            parser.program();
            CheckValid cv = new CheckValid();
            if (cv.hasError()) {
                System.out.println("Not accepted");
            } else {
                System.out.println("Accepted");
            }
        }
    }
}