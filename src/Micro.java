import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jcp.xml.dsig.internal.SignerOutputStream;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class Micro {
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            String filename = args[i];
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
            //printSymbolTable(data.getTable());
            //printTiny(data.getCodeList());



        }
    }
    private static void printTiny(ArrayList<Code> codelist){
        int register_count = 0;
        ArrayList<Register> registerList = new ArrayList<>();
        for (Code c: codelist){
            Register register = new Register(register_count,c.getResult());
            String opcode = c.getOpcode();
            // One address Code, no register involves.
            if (opcode == "WRITEI"){
                System.out.println("sys writei "+c.getResult());
            }
            else if (opcode == "WRITEF"){
                System.out.println("sys writer "+c.getResult());
            }
            // Two address Code, register involves.
            else if (opcode == "STOREI" || opcode== "STOREF"){
                // if op1 doesn't start with "$T", create a new register and append to register list
                if(c.getOp1().startsWith("$T") == false){

                    System.out.println("move " + c.getOp1() +" "+ register.toStirng());
                    register.add(c.getResult());
                    registerList.add(register);
                    register_count++;

                }
                else if (c.getOp1().startsWith("$T")){
                    System.out.println("move " +checkDollar(registerList, c.getOp1())+" "+c.getResult());
                }



            }
            // Three address Code, more complicated. we have
            else if (opcode == "MULTF"){

                // muli a
                if(c.getOp1().startsWith("$T") == false){
                    System.out.println("move " + c.getOp1() + " " + register.toStirng());
                    // a b
                    if (c.getOp2().startsWith("$T") == false){
                        System.out.println("mulr "+ c.getOp2()+" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    // muli a $T0
                    else{
                        System.out.println("mulr " +checkDollar(registerList, c.getOp2()) +" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    register_count ++;

                }
                // muli $T
                else{
                    // muli $T0 a
                    if(c.getOp2().startsWith("$T") == false){
                        System.out.println("mulr " + c.getOp2() + " "+ checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r",""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);
                    }
                    // muli $T0 $T1
                    else {
                        System.out.println("mulr " + checkDollar(registerList, c.getOp2()) + " " + checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r", ""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);
                    }
                }
                registerList.add(register);
            }
            else if (opcode == "ADDF"){

                // muli a
                if(c.getOp1().startsWith("$T") == false){
                    System.out.println("move " + c.getOp1() + " " + register.toStirng());
                    // a b
                    if (c.getOp2().startsWith("$T") == false){
                        System.out.println("addr "+ c.getOp2()+" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    // muli a $T0
                    else{
                        System.out.println("addr " +checkDollar(registerList, c.getOp2()) +" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    register_count ++;

                }
                // muli $T
                else{
                    // muli $T0 a
                    if(c.getOp2().startsWith("$T") == false){
                        System.out.println("addr " + c.getOp2() + " "+ checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r",""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);
                    }
                    // muli $T0 $T1
                    else {
                        System.out.println("addr " + checkDollar(registerList, c.getOp2()) + " " + checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r", ""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);
                    }
                }
                registerList.add(register);
            }
            else if (opcode == "SUBI"){

                // muli a
                if(c.getOp1().startsWith("$T") == false){
                    System.out.println("move " + c.getOp1() + " " + register.toStirng());
                    // a b
                    if (c.getOp2().startsWith("$T") == false){
                        System.out.println("subi "+ c.getOp2()+" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    // muli a $T0
                    else{
                        System.out.println("subi " +checkDollar(registerList, c.getOp2()) +" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    register_count ++;

                }
                // muli $T
                else{
                    // muli $T0 a
                    if(c.getOp2().startsWith("$T") == false){
                        System.out.println("subi " + c.getOp2() + " "+ checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r",""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);
                    }
                    // muli $T0 $T1
                    else {
                        System.out.println("subi " + checkDollar(registerList, c.getOp2()) + " " + checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r", ""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);
                    }
                }
                registerList.add(register);
            }
            else if (opcode == "MULTI"){

                // muli a
                if(c.getOp1().startsWith("$T") == false){
                    System.out.println("move " + c.getOp1() + " " + register.toStirng());
                    // a b
                    if (c.getOp2().startsWith("$T") == false){
                        System.out.println("muli "+ c.getOp2()+" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    // muli a $T0
                    else{
                        System.out.println("muli " +checkDollar(registerList, c.getOp2()) +" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    register_count ++;

                }
                // muli $T
                else{
                    // muli $T0 a
                    if(c.getOp2().startsWith("$T") == false){
                        System.out.println("muli " + c.getOp2() + " "+ checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r",""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);
                    }
                    // muli $T0 $T1
                    else {
                        System.out.println("muli " + checkDollar(registerList, c.getOp2()) + " " + checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r", ""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);
                    }
                }
                registerList.add(register);
            }

            else if (opcode == "ADDI"){

                // addi a
                if(c.getOp1().startsWith("$T") == false){

                    System.out.println("move "+ c.getOp1() + " " + register.toStirng());
                    // a b
                    if (c.getOp2().startsWith("$T") == false){
                        System.out.println("addi "+ c.getOp2()+" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    // addi a $T0
                    else{
                        System.out.println("addi " +checkDollar(registerList, c.getOp2()) +" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    register_count ++;

                }
                // addi $T
                else{
                    // addi $T0 a
                    if(c.getOp2().startsWith("$T") == false){
                        System.out.println("addi " + c.getOp2() + " " + checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r", ""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);
                    }
                    // addi $T0 $T1
                    else {
                        System.out.println("addi " + checkDollar(registerList, c.getOp2()) + " " + checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r", ""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);

                        //register.add(c.getResult());
                    }
                }
                registerList.add(register);
            }
            else if (opcode == "DIVI"){

                // divi a
                if(c.getOp1().startsWith("$T") == false){

                    System.out.println("move " + c.getOp1() + " " + register.toStirng());
                    // a b
                    if (c.getOp2().startsWith("$T") == false){
                        System.out.println("divi "+ c.getOp2()+" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    // divi a $T0
                    else{
                        System.out.println("divi " +checkDollar(registerList, c.getOp2()) +" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    register_count ++;

                }
                // divi $T
                else{
                    // divi $T0 a
                    if(c.getOp2().startsWith("$T") == false){
                        System.out.println("divi " + c.getOp2() + " "+ checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r",""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);
                    }
                    // divi $T0 $T1
                    else {
                        System.out.println("divi " + checkDollar(registerList, c.getOp2()) + " " + checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r", ""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);

                    }
                }
                registerList.add(register);
            }
            else if (opcode == "DIVF"){

                // divi a
                if(c.getOp1().startsWith("$T") == false){

                    System.out.println("move " + c.getOp1() + " " + register.toStirng());
                    // a b
                    if (c.getOp2().startsWith("$T") == false){
                        System.out.println("divr "+ c.getOp2()+" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    // divi a $T0
                    else{
                        System.out.println("divr " +checkDollar(registerList, c.getOp2()) +" "+ register.toStirng());
                        register.add(c.getResult());
                    }
                    register_count ++;

                }
                // divi $T
                else{
                    // divi $T0 a
                    if(c.getOp2().startsWith("$T") == false){
                        System.out.println("divr " + c.getOp2() + " "+ checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r",""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);
                    }
                    // divi $T0 $T1
                    else {
                        System.out.println("divr " + checkDollar(registerList, c.getOp2()) + " " + checkDollar(registerList, c.getOp1()));
                        String prev_reg = checkDollar(registerList, c.getOp1());
                        int reg_count = Integer.parseInt(prev_reg.replace("r", ""));
                        Register test_reg = new Register(reg_count,c.getResult());
                        registerList.add(test_reg);

                    }
                }
                registerList.add(register);
            }

        }
        System.out.println("sys halt");
    }


    private static String checkDollar( ArrayList<Register> Register_list,String opcode){
        for (Register register : Register_list){
            if(register.getlist().contains(opcode)){
                return register.toStirng();
            }

        }

        return "opcode";
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
            System.out.println();
            printSymbolTable(st);
        }
    }

}