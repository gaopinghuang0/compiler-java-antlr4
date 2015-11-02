import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Micro {
    public static void main(String[] args) throws IOException {
        for (String filename : args) {
            ANTLRFileStream input = null;
            try {
                input = new ANTLRFileStream(filename);
            } catch (FileNotFoundException e) {
                System.err.println("Fatal IO error:\n" + e);
                System.exit(1);
            }
            MicroLexer lexer = new MicroLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MicroParser parser = new MicroParser(tokens);
            ReturnData data = parser.program().data;
            printIR(data.getCodeList());
            System.out.println(";tiny code");
            printSymbolTable(data.getTable());
            TinyCode tc = new TinyCode(data.getCodeList());
            tc.toTinyCode();
        }
    }

    private static void printIR(ArrayList<Code> codeList) {
        System.out.println(";IR code");
        for (Code c : codeList) {
            System.out.println(";"+c.toIR());
        }
    }

    private static void printSymbolTable(SymbolTable table) {
        table.printTable();
        for (SymbolTable st : table.getChildren()) {
            printSymbolTable(st);
        }
    }

}