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

            System.out.println(";IR code");
            printIRcode(data.getTable());

            updateLivenessAnalysis(data.getTable());

            System.out.println(";tiny code");
            printSymbolTable(data.getTable());

            printPreTiny();
            printTinyCode(data.getTable());
            System.out.println("end");
        }
    }


    private static void printIRcode(SymbolTable table) {
        table.printIR();

        for (SymbolTable st : table.getChildren()) {
            printIRcode(st);
        }
    }

    private static void updateLivenessAnalysis(SymbolTable table) {
        table.updateLiveness();

        for (SymbolTable st : table.getChildren()) {
            updateLivenessAnalysis(st);
        }
    }


    private static void printSymbolTable(SymbolTable table) {
        table.printTable();

        for (SymbolTable st : table.getChildren()) {
            // do not print block-level codeList
            if (st.getClass() != Block.class) {
                printSymbolTable(st);
            }
        }
    }

    // add before main function
    private static void printPreTiny() {
        System.out.println("push");
        for (int i = 0; i < 4; i++) {
            System.out.println("push " + "r" + i);
        }
        System.out.println("jsr main");
        System.out.println("sys halt");
    }

    private static void printTinyCode(SymbolTable table) {
        table.printTiny(table.getParamId(), table.getDeclId());

        for (SymbolTable st : table.getChildren()) {
            printTinyCode(st);
        }
    }
}