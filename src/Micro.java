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
            SymbolTable table = data.getTable();

            System.out.println(";IR code");
            printIRcode(table);

            List<String> globalTemp = getGlobalTemp(table);
            // use for live analysis, without this function call, we can't get live analysis
            updateLiveness(table, globalTemp);

            System.out.println(";tiny code");
            printSymbolTable(table);

            printPreTiny();
            printTinyCode(table);
            System.out.println("end");
        }
    }

    // temporary function, use to look how codelist looks like
    private static void printCode(SymbolTable table){
        table.printCodeOut();
        for(SymbolTable st: table.getChildren()){
            printCode(st);
        }
    }

    private static List<String> getGlobalTemp(SymbolTable table) {
        List<String> globalTemp = new ArrayList<>();
        if (table.getScope().equals("GLOBAL")) {
            System.out.println();
            for (SymbolEntry se: table.getDecls()) {
                // both parameter variable and local variable are in decl list
                // but parameter variable starts with "$P" while the other "$L"
                String variable = se.getVariable();
                if (variable.startsWith("$L")) {
                    globalTemp.add(variable);
                }
            }
        }
        return globalTemp;
    }

    private static void printIRcode(SymbolTable table) {
        table.printIR();

        for (SymbolTable st : table.getChildren()) {
            if (st.getClass() != Block.class) {
                printIRcode(st);
            }
        }
    }

    private static void updateLiveness(SymbolTable table, List<String> globalTemp) {
        table.doLivenessAnalysis(globalTemp);

        for (SymbolTable st : table.getChildren()) {
            if (st.getClass() != Block.class) {
                updateLiveness(st, globalTemp);
            }
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
        table.printTiny(table.getParamId(), table.getLocalTemp(), table.getDeclId());

        for (SymbolTable st : table.getChildren()) {
            // do not print block-level codeList since its code is already
            // appended to function-level codeList
            if (st.getClass() != Block.class) {
                printTinyCode(st);
                System.out.println(";Spilling registers at the end of the Basic Block");
            }
        }
    }
}