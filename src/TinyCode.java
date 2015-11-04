import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by hgp on 10/18/2015.
 */
public class TinyCode {
    private ArrayList<Code> codeList;
    private int currReg = 0;
    private HashMap<String, String> map;

    public TinyCode(ArrayList<Code> codeList) {
        this.codeList = codeList;
        this.map = new HashMap<>();
    }

    public ArrayList<Code> getCodeList() {
        return codeList;
    }

    public int getCurrReg() {
        return currReg;
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public String nextReg() {
        return "r" + (currReg++);
    }

    public String lookUpMap(String op) {
        String reg = map.get(op);
        if (reg == null) {
            System.err.println("Not found in the map");
        }
        return reg;
    }

    public void toTinyCode() {
        for (Code c : codeList) {
            if (c.getClass() == OneAddressCode.class) {
                handleOneAddress(c);  // write and read
            } else if (c.getClass() == TwoAddressCode.class) {
                handleTwoAddress(c);  // store
            } else {
                handleThreeAddress(c);
            }
        }
        System.out.println("sys halt");
    }

    public void handleOneAddress(Code c) {
        //LABEL JUMP READ WRITE
        String op = c.getOpcode();

        switch (op) {
            case "LABEL":
                System.out.println(getTinyOpcode(op) + " " + c.getResult());
                break;
            case "JUMP":
                System.out.println( getTinyOpcode(op).replace("u","") + " " +c.getResult());
                break;
            default:
                System.out.println("sys "+ getTinyOpcode(c.getOpcode()) + " " + c.getResult());
                break;
        }
    }

    public void handleTwoAddress(Code c) {
        String op1 = c.getOp1();
        String result = c.getResult();
        String reg = "";
        if (op1.startsWith("$T")) {
            reg = lookUpMap(op1);
            System.out.println("move "+reg+ " " +c.getResult());
        } else {
            reg = nextReg();
            System.out.println("move " + op1 + " " + reg);
            if(!result.startsWith("$T")){
                System.out.println("move "+reg+" "+result);
            }
            map.put(c.getResult(), reg);
        }
    }

    public void handleThreeAddress(Code c) {
        String op1 = c.getOp1();
        String op2 = c.getOp2();
        String op = c.getOpcode();
        String type = c.getType();
        String reg = "";
        String[] operationList = {"ADDI","ADDF","SUBI","SUBF","MULTI","MULTF","DIVI","DIVF"};
        if (Arrays.asList(operationList).contains(op)){
            if (op1.startsWith("$T")) {
                reg = lookUpMap(op1);
            } else {
                reg = nextReg();
                System.out.println("move " + op1 + " " + reg);
            }
            op2 = op2.startsWith("$T") ? lookUpMap(op2) : op2;
            System.out.println(getTinyOpcode(c.getOpcode()) + " " + op2 + " " + reg);
            map.put(c.getResult(), reg);
        } else {
            if (!(op2.startsWith("$T") || op2.startsWith("r"))){
                reg = nextReg();
                System.out.println("move " + op2 + " " + reg);
                map.put(op2, reg);
            }
            op2 = op2.startsWith("r") ? op2 : lookUpMap(op2);

            if (type.equals("INT")){
                System.out.println("cmpi " + op1 + " " + op2);
            }
            else if(type.equals("FLOAT")){
                System.out.println("cmpr " + op1 + " " + op2);
            }
            System.out.println("j" + op.toLowerCase() + " " + c.getResult());
        }

    }

    public String getTinyOpcode(String opcode) {
        String op = opcode.substring(0, 3).toLowerCase();
        if (op.equals("mul") || op.equals("div") || op.equals("add") || op.equals("sub")) {
            op += opcode.endsWith("F") ? "r" : "i";
        } else if (opcode.startsWith("READ")) {
            if (opcode.endsWith("F")){
                op = "readr";
            } else if (opcode.endsWith("I")){
                op = "readi";
            } else {
                op = "reads";
            }
        } else if (opcode.startsWith("WRITE")) {
            if (opcode.endsWith("F")){
                op = "writer";
            } else if (opcode.endsWith("I")){
                op = "writei";
            } else {
                op = "writes";
            }
        } else if (opcode.equals("LABEL") || opcode.equals("JUMP")){
            op = opcode.toLowerCase();
        }

        return op;
    }
}
