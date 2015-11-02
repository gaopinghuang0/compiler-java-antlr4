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
        System.out.println("sys "+ getTinyOpcode(c.getOpcode()) + " " + c.getResult());
    }

    public void handleTwoAddress(Code c) {
        String op1 = c.getOp1();
        String reg = "";
        if (op1.startsWith("$T")) {
            reg = lookUpMap(op1);
            System.out.println("move "+reg+ " " +c.getResult());
        } else {
            reg = nextReg();
            System.out.println("move "+op1+" "+reg);
            map.put(c.getResult(), reg);
        }
    }

    public void handleThreeAddress(Code c) {
        String op1 = c.getOp1();
        String op2 = c.getOp2();
        String reg = "";

        if (op1.startsWith("$T")) {
            reg = lookUpMap(op1);
        } else {
            reg = nextReg();
            System.out.println("move " + op1 + " " + reg);
        }
        op2 = op2.startsWith("$T") ? lookUpMap(op2) : op2;
        System.out.println(getTinyOpcode(c.getOpcode()) + " " + op2 + " " + reg);
        map.put(c.getResult(), reg);
    }

    public String getTinyOpcode(String opcode) {
        String op = opcode.substring(0, 3).toLowerCase();
        if (op.equals("mul") || op.equals("div") || op.equals("add") || op.equals("sub")) {
            op += opcode.endsWith("F") ? "r" : "i";
        } else if (opcode.startsWith("READ")) {
            op = opcode.endsWith("F") ? "readr" : "readi";
        } else if (opcode.startsWith("WRITE")) {
            op = opcode.endsWith("F") ? "writer" : "writei";
        }

        return op;
    }
}