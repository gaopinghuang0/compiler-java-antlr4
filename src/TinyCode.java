import java.util.*;

/**
 * Created by hgp on 10/18/2015.
 */
public class TinyCode {
    private List<Code> codeList;
    private int currReg = 0;
    private HashMap<String, String> map;
    private int paramId;
    private int declId;
    public TinyCode(List<Code> codeList,int paramId,int declId) {
        this.codeList = codeList;
        this.map = new HashMap<>();
        this.paramId = paramId;
        this.declId = declId;
    }

    public List<Code> getCodeList() {
        return codeList;
    }

    public int getCurrReg() {
        return currReg;
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public String getNextReg() {
        if (currReg <= 3) {
            return "r" + (currReg++);
        } else {
            return GlobalTinyCode.getNextReg();
        }
    }

    public String lookUpMap(String op) {
        String reg = map.get(op);
        if (reg == null) {
//            System.err.println("Not found in the map" );
        }
        return reg;
    }

    public void toTinyCode() {
        for (Code c : codeList) {
            if (c.getClass() == OneAddressCode.class) {
                handleOneAddress(c);  // write and read...
            } else if (c.getClass() == TwoAddressCode.class) {
                handleTwoAddress(c);  // store
            } else {
                handleThreeAddress(c);
            }
        }
    }

    public void handleOneAddress(Code c) {
        //LABEL JUMP READ WRITE ...
        String op = c.getOpcode();
        String result = c.getResult();
        switch (op) {
            case "LABEL":
                System.out.println(getTinyOpcode(op) + " " + result);
                break;
            case "JUMP":
                System.out.println(getTinyOpcode(op).replace("u", "") + " " + result);
                break;
            case "PUSH":
            case "POP":
                System.out.println(op.toLowerCase() + " " + getTinyTransform(result));
                break;
            case "JSR":
                for(int i=0; i<4; i++) {
                    System.out.println("push " + "r" + i);
                }
                System.out.println(op.toLowerCase() + " " + result);
                for(int i=3; i>-1; i--) {
                    System.out.println("pop " + "r" + i);
                }
                break;
            case "RET":
                System.out.println("unlnk");
                System.out.println(getTinyOpcode(c.getOpcode()));
                break;
            case "LINK":
                System.out.println(op.toLowerCase() + " " + (declId - 1));
                break;
            default:  // READ or WRITE
                System.out.println("sys "+ getTinyOpcode(c.getOpcode()) + " " + getTinyTransform(result));
                break;
        }
    }

    public void handleTwoAddress(Code c) {
        String op1 = c.getOp1();
        String result = c.getResult();
        String reg = "";
        if(op1.startsWith("$L")) {
            reg = getNextReg();
            System.out.println("move " + getTinyTransform(op1) + " "+reg);
            System.out.println("move " + reg + " " + getTinyTransform(result));
            map.put(op1,reg);
        }else {
            System.out.println("move " + getTinyTransform(op1) + " " + getTinyTransform(result));
        }
//        if (op1.startsWith("$T")) {
//            reg = lookUpMap(op1);
//            System.out.println("move " + reg + " " + c.getResult());
//        } else {
//            reg = nextReg();
//            System.out.println("move " + op1 + " " + reg);
//            if(!result.startsWith("$T")){
//                System.out.println("move "+reg+" "+result);
//            }
//            map.put(c.getResult(), reg);
//        }
    }

    public void handleThreeAddress(Code c) {
        String op1 = c.getOp1();
        String op2 = c.getOp2();
        String op = c.getOpcode();
        String type = c.getType();
        String result = c.getResult();
        String reg = "";
        String[] operationList = {"ADDI","ADDF","SUBI","SUBF","MULTI","MULTF","DIVI","DIVF"};
        if (Arrays.asList(operationList).contains(op)){
            System.out.println("move " + getTinyTransform(op1) +" "+ getTinyTransform(result));
            System.out.println(getTinyOpcode(op)+" " + getTinyTransform(op2)+ " " + getTinyTransform(result));
//            if (op1.startsWith("$T")) {
//                reg = lookUpMap(op1);
//            } else {
//                reg = getNextReg();
//                System.out.println("move " + op1 + " " + reg);
//            }
//            op2 = op2.startsWith("$T") ? lookUpMap(op2) : op2;
//            System.out.println(getTinyOpcode(c.getOpcode()) + " " + op2 + " " + reg);
//            map.put(c.getResult(), reg);
        } else {
//            if (!(op2.startsWith("$T") || op2.startsWith("r"))){
//                reg = getNextReg();
//                System.out.println("move " + op2 + " " + reg);
//                map.put(op2, reg);
//            }
//            op2 = op2.startsWith("r") ? op2 : lookUpMap(op2);
//
            if(op2.startsWith("$L")) {
                reg = getNextReg();
                System.out.println("move " + getTinyTransform(op2) + " " + reg);
                if (type.equals("INT")){
                    System.out.println("cmpi " + getTinyTransform(op1) + " " + reg);
                }
                else if(type.equals("FLOAT")){
                    System.out.println("cmpr " + getTinyTransform(op1) + " " + reg);
                }
                System.out.println("j" + op.toLowerCase() + " " + getTinyTransform(c.getResult()));
            }else{

                if (type.equals("INT")){
                    System.out.println("cmpi " + getTinyTransform(op1) + " " + getTinyTransform(op2));
                }
                else if(type.equals("FLOAT")){
                    System.out.println("cmpr " + getTinyTransform(op1) + " " + getTinyTransform(op2));
                }
                System.out.println("j" + op.toLowerCase() + " " + getTinyTransform(c.getResult()));
            }
        }

    }

    public String getTinyTransform(String s){
        String result = s;
        if (s.startsWith("$L")) {
            result = "$-" + s.replace("$L", "");
        } else if (s.startsWith("$T")) {
            // some lookup methods for register...
            result = lookUpMap(s);
            if (result == null) {
                result = getNextReg();
                map.put(s, result);
            }
        } else if (s.startsWith("$P")) {
            int temp = Integer.parseInt(s.replace("$P", ""));
            temp = paramId + 5 - temp;
            result = "$" + temp;
        } else if (s.startsWith("$R")) {
            result = "$" + (5 + paramId);
        }

        return result;
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
