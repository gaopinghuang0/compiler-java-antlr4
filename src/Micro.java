import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;

public class Micro {
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            String filename = args[i];
            ANTLRFileStream input = new ANTLRFileStream(filename);
            MicroLexer lexer = new MicroLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MicroParser parser = new MicroParser(tokens);
            SymbolTable table = parser.program().table;
            printSymbolTable(table);
        }
    }

    private static void printSymbolTable(SymbolTable table) {
        table.printTable();
        for (SymbolTable st : table.getChildren()) {
            System.out.println();
            printSymbolTable(st);
        }
    }
}