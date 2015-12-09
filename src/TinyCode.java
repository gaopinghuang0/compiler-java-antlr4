
import java.util.*;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by hgp on 10/18/2015.
 */
public class TinyCode {
    private List<Code> codeList;
    private int currReg = 0;
    private HashMap<String, String> map;
    private List<String> dirtyList;
    private int paramId;
    private int localTemp;
    private int declId;
    private boolean isDebug = true;


    public TinyCode(List<Code> codeList,int paramId,int localTemp, int declId) {
        this.codeList = codeList;
        this.paramId = paramId;
        this.localTemp = localTemp;
        this.declId = declId;
        this.map = new HashMap<String, String>();
        this.dirtyList = new ArrayList<String>();
        initMap();
    }

    public List<Code> getCodeList() {
        return codeList;
    }

    public int getCurrReg() {
        return currReg;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void spillAllReg(){
        for(String key : map.keySet()) {
            String value = map.get(key);
            if (!value.equals("null")) {
                if (checkDirty(value)) {
                    spillReg(key, value);
                }
                map.put(key, "null");
            }
        }
    }

    public void spillReg(String key, String value) {
        dbgPrint(";Spilling variable: " + value);
        System.out.println("move " + key + " " +getTinyTransform(value));
        dirtyList.remove(value);
    }

    public void initMap() {
        for (int i=0; i<4; i++){
            map.put("r"+i, "null");
        }
    }

    public String getNextReg() {
        if (currReg <= 3) {
            return "r" + (currReg++);
        } else {
            return GlobalTinyCode.getNextReg();
        }
    }

    public void toTinyCode() {
        for (Code c : codeList) {
            if (c.getClass() == OneAddressCode.class) {
                handleOneAddress(c);  // write and read...
            } else if (c.getClass() == TwoAddressCode.class) {
                //System.out.println(this.map);
                handleTwoAddress(c);  // store
            } else {
                handleThreeAddress(c);
            }
        }
        dbgPrint("; Final map" + map);
    }

    public void handleOneAddress(Code c) {
        // LABEL JUMP READ WRITE ...
        String op = c.getOpcode();
        String result = c.getResult();
        Set<String> liveness = c.getOut();
        String reg = "";
        switch (op) {
            case "LABEL":
                System.out.println(getTinyOpcode(op) + " " + result);
                break;
            case "JUMP":
                System.out.println(getTinyOpcode(op).replace("u", "") + " " + result);
                break;
            case "JSR":
                spillAllReg();
                for(int i=0; i<4; i++) {
                    System.out.println("push " + "r" + i);
                }
                System.out.println(op.toLowerCase() + " " + result);
                for(int i=3; i>-1; i--) {
                    System.out.println("pop " + "r" + i);
                }
                break;
            case "RET":
                // at the end of each jsr function call, free all reg map
                spillAllReg();

                System.out.println("unlnk");
                System.out.println(getTinyOpcode(c.getOpcode()));
                break;
            case "LINK":
                //System.out.println(op.toLowerCase() + " " + (declId));
                int resultValue = localTemp + declId - 2;
                System.out.println(op.toLowerCase() + " " + resultValue);
                break;
            default:  // READ or WRITE
                dbgPrint(";" + c + " " +liveness);
                if (op.contains("READ") || op.contains("WRITEI") || op.contains("WRITEF") || op.contains("POP") || op.contains("PUSH")) {

                    reg = regEnsure(c, result, liveness);

                    if (op.contains("READ") || op.contains("POP")) {
                        addDirty(result);
                    }

                    if (op.contains("POP") || op.contains("PUSH")) {
                        System.out.println(getTinyOpcode(op) + " " + reg);
                    } else {
                        System.out.println(";sys " + map);
                        System.out.println("sys " + getTinyOpcode(op) + " " + reg);
                    }
                } else {  // hgp: what's the else situation?
                    System.out.println("sys " + getTinyOpcode(c.getOpcode()) + " " + getTinyTransform(result));
                }
                break;
        }
    }

    public void handleTwoAddress(Code c) {
        String op1 = c.getOp1();
        String result = c.getResult();
        String reg1 = "";
        String reg2 = "";
        Set<String> liveness = c.getOut();

        // ensure(): $L1 has register r2
        // move r2 $8
        // if(op1.startswith("$L")
        if (result.equals("$R")) {
            reg1 = regEnsure(c, op1, liveness);
            // $R does not need to allocate, since it has special location
            System.out.println("move " + reg1 + " " + getTinyTransform(result));
        } else {  // STORE
            dbgPrint(";" + c + " "+liveness);
            reg1 = regEnsure(c, op1, liveness);
            checkRegLive(liveness);
            reg2 = regAllocate(c, result, liveness);
            if (reg2.equals(reg1)) {
                dbgPrint(";Switching owner of register " + reg2 + " to " + result);
            } else {
                System.out.println("move " + reg1 + " " + reg2);
            }
            addDirty(result);
            //System.out.println("move " + getTinyTransform(op1) + " " + getTinyTransform(result));
        }
    }

    public void handleThreeAddress(Code c) {
        String op1 = c.getOp1();
        String op2 = c.getOp2();
        String op = c.getOpcode();
        String type = c.getType();
        String result = c.getResult();
        Set<String> liveness = c.getOut();
        String reg1, reg2, reg3;
        String[] operationList = {"ADDI","ADDF","SUBI","SUBF","MULTI","MULTF","DIVI","DIVF"};
        if (Arrays.asList(operationList).contains(op)){
            dbgPrint(";"+c +" " + liveness);
            reg1 = regEnsure(c, op1, liveness);
            reg2 = regEnsure(c, op2, liveness);
            checkRegLive(liveness);
            reg3 = regAllocate(c, result, liveness);

            if (reg3.equals(reg1)) {
                dbgPrint(";Switching owner of register " + reg3 + " to " + result);
                System.out.println(getTinyOpcode(op) + " " + reg2 + " " + reg1);
            } else {
                System.out.println(getTinyOpcode(op) + " " + reg2 + " " + reg1);
                System.out.println("move " + reg1 + " " + reg3 );
            }

            addDirty(result);
        } else if (Arrays.asList(Compop.IRop).contains(op)) {  // compare op
            dbgPrint(";" + c + " " + liveness);
            reg1 = regEnsure(c, op1, liveness);
            reg2 = regEnsure(c, op2, liveness);
            checkRegLive(liveness);
            if (type.equals("INT")) {
                System.out.println("cmpi " + reg1 + " " + reg2);
            } else if (type.equals("FLOAT")) {
                System.out.println("cmpr" + reg1 + " " + reg2);
            }

            // no allocate or mark dirty for result label
            System.out.println("j" + op.toLowerCase() + " " + getTinyTransform(result));
        }
    }

    public String regEnsure(Code c, String op, Set<String> liveness) {
        // check if opr already has regiser
        if (!op.startsWith("$")) return op;

        for (String key : map.keySet()) {
            String value = map.get(key);
            // reg is found
            if (value.equals(op)) {
                dbgPrint(";ensure(): " + op + " has register " + key + " " + map);
                return key;
            }
        }

        // reg not found, allocate a reg for op
        // if op is not result, we need to load it to reg
        String reg = regAllocate(c, op, liveness);;
        dbgPrint(";loading " + op + " to register " + reg);
        System.out.println("move " + getTinyTransform(op) + " " + reg);
        map.put(reg, op);

        return reg;
    }

    public String regAllocate(Code c, String op, Set<String> liveness){
        // if there is a free reg, choose reg
        for (String key: map.keySet()){
            String hashValue = map.get(key);
            if (hashValue.equals("null")){
                map.put(key, op);
                dbgPrint(";ensure(): " + op + " gets register " + key + " " + map);
                return key;
            }
        }

        // choose a reg to free
        // select reg that is not in liveness to spill
        String reg ="";
        List<String> opList = c.getOpArray();
        for (String key : map.keySet()) {
            String value = map.get(key);
            if (!opList.contains(value)) {
                reg = key;
                if (!liveness.contains(value)) {
                    break;
                }
            }
        }

        // free and mark as associated
        regFree(map.get(reg), liveness);
        map.put(reg, op);
        return reg;
    }

    public void regFree(String op, Set<String> liveness) {
        // if r is marked dirty and live
        // generate store
        // mark r as free
        if (!liveness.contains(op)) {
            dbgPrint(";Freeing unused variable " + op + " " + map);
        }
        String key = regLookup(op);
        map.put(key, "null");
        if (dirtyList.contains(op)) {
            spillReg(key, op);
        }
    }

    public String regLookup(String op){
        for( String key : this.map.keySet()){
            String value = this.map.get(key);
            if (op.equals(value)){return key;}
        }
        return null;
    }

    public void checkRegLive(Set<String> liveness){
        for(String key : map.keySet()) {
            String value = map.get(key);
            if (!value.equals("null") && !liveness.contains(value)) {
                regFree(value, liveness);
            }
        }
    }

    public void addDirty(String result) {
        if (!checkDirty(result)) {
            dirtyList.add(result);
        }
    }

    public boolean checkDirty(String value){
        return this.dirtyList.contains(value);
    }

    public String getTinyTransform(String s){
        String result = s;
        if (s.startsWith("$L")) {
            result = "$-" + s.replace("$L", "");
        } else if (s.startsWith("$T")) {
            // some lookup methods for register...
            int resultValue = Integer.parseInt(s.replace("$T","")) + declId - 1;
            result = "$-" + resultValue;
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
        } else if (opcode.equals("POP") || opcode.equals("PUSH")){
            op = opcode.toLowerCase();
        }

        return op;
    }

    public void dbgPrint(String msg) {
        if (isDebug) {
            System.out.println(msg);
        }
    }
}
