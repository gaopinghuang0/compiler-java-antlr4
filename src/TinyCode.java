
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
            if (!value.equals("null")){
                dbgPrint(";Spilling variable: " + value);
                System.out.println("move " + key + " " +getTinyTransform(value));
                map.put(key, "null");
            }
        }
    }

    public void initMap(){
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

    public String regEnsure(Code c, String op, Set<String> liveness, boolean isFirst) {
        // check if opr already has regiser
        if (!op.startsWith("$")) return op;
        String reg = "";
        for (String key : map.keySet()) {
            String value = map.get(key);
            // reg found
            if (value.equals(op)) {
                reg = key;
                dbgPrint(";ensure(): " + op + " has register " + reg + " " + map);
                break;
            }
        }
        // reg not found, allocate a reg for op
        if (reg.isEmpty()) {
            reg = regAllocate(c, op, liveness);
            //|| c.getOp1().equals(op)
            // TODO: handle op1 in STORE case, "loading"
            if (c.getClass() == ThreeAddressCode.class || c.getOpcode().contains("WRITE")) {
                dbgPrint(";loading " + op + " to register " + reg);
                System.out.println("move " + getTinyTransform(op) + " " + reg);
                map.put(reg, op);
            } else if (c.getOpcode().contains("STORE") ) {
                if (isFirst) {
                    dbgPrint(";loading " + op + " to register " + reg);
                    map.put(reg, op);
                    System.out.println("move " + getTinyTransform(op) + " " + reg);
                }
            }
        }
        return reg;
    }

    public String regAllocate(Code c, String op, Set<String> liveness){
        String reg ="";
        // if there is a free reg, choose reg
        for (int i =3; i >=0; i--){
            String hashKey = "r"+i;
            String hashValue = this.map.get(hashKey);
            if (hashValue.equals("null")){
                this.map.put(hashKey,op);
                reg = hashKey;
                dbgPrint(";ensure(): " + op + " gets register " + reg + " " + this.map);
                return reg;
            }

        }

        // select reg that is not in liveness to spill
        boolean regPick = false;
        ArrayList<String> opList = c.getOpArray();
        for(String key : map.keySet()){
            String value = map.get(key);
            //System.out.println(opList);
            if ((!liveness.contains(value)) && (!opList.contains(value))) {
                dbgPrint(";allocate() has to spill " + value);
                dbgPrint(";Spilling variable: " + value);
                System.out.println("move " + key + " " + getTinyTransform(value));
                this.map.put(key, op);
                dbgPrint(";ensure(): " + op + " gets register " + reg + " " + this.map);
                dirtyList.remove(value);
                reg = key;
                regPick = true;
                break;
            }
        }
        if(!regPick) {
            // randomly pick one variable to spill, but not op
            // search from livenss set, backward... and also in reg
            int liveLen = liveness.size();
            List<String> list = new ArrayList<String>(liveness);
            for(int index=liveLen; index >0; index--) {
                String value = list.get(index-1);
                String checkReg = regLookup(value);
                if (checkReg !=null) {
                    if (!opList.contains(value)) {
                        dbgPrint(";allocate() has to spill " + value);
                        String key = regLookup(value);
                        this.map.put(key, "null");
                        if (dirtyList.contains(value)) {
                            dbgPrint(";Spilling variable: " + value);
                            System.out.println("move " + key + " " + getTinyTransform(value));
                            dirtyList.remove(value);
                            map.put(key, op);
                            dbgPrint(";ensure(): " + op + " gets register " + key + " " + this.map);

                        }
                        //reg = regEnsure(c, op, liveness, isFirst);
                        reg = key;
                        break;
                    }
                }
            }
        }
        return reg;
    }

    public void regFree(String op, Set<String> liveness){
        // if r is marked dirty and live
        // generate store
        // mark r as free
        dbgPrint(";Freeing unused variable " + op + " " + map);
        String key = regLookup(op);
        this.map.put(key,"null");
        if (this.dirtyList.contains(op)) {
            dbgPrint(";Spilling variable: " + op);
            System.out.println("move " + key + " " + getTinyTransform(op));
            this.dirtyList.remove(op);
        }
    }

    public String regLookup(String op){
        for( String key : this.map.keySet()){
            String value = this.map.get(key);
            if (op.equals(value)){return key;}
        }
        return null;
    }

    public void checkLive(String op, Set<String> liveness){
        if (!liveness.contains(op)){
            regFree(op, liveness);
        }
    }

    public void checkRegLive(Set<String> liveness){
        for(String key : map.keySet()) {
            String value = map.get(key);
            if(!value.equals("null")){ checkLive(value,liveness);}
        }
    }

    public void switchReg(String op1, String result){
        String reg = regLookup(op1);
        this.map.put(reg, result);
        dbgPrint(";Switching owner of register " + reg + " to " + result);
        if (this.dirtyList.contains(op1)) {
            dbgPrint(";Spilling variable: " + op1);
            System.out.println("move " + reg + " " + getTinyTransform(op1));
            this.dirtyList.remove(op1);
        }
        dirtyList.add(result);
    }
    public boolean checkDirty(String dirty){
        return this.dirtyList.contains(dirty);
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
                dbgPrint(";Spilling registers at the end of the Basic Block" + map);
                spillAllReg();
                System.out.println(getTinyOpcode(op).replace("u", "") + " " + result);
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

                //checkRegLive(liveness);
                System.out.println("unlnk");
                System.out.println(getTinyOpcode(c.getOpcode()));
                break;
            case "LINK":
                //System.out.println(op.toLowerCase() + " " + (declId));
                int resultValue = localTemp + declId -2;
                System.out.println(op.toLowerCase() + " " + resultValue);

                break;
            default:  // READ or WRITE
                dbgPrint(";" + c + " " +liveness);
                if(op.contains("READ") || op.contains("WRITEI") || op.contains("WRITEF") || op.contains("POP") || op.contains("PUSH")){
                    boolean isFirst = false;

                    reg = regEnsure(c, result, liveness, isFirst);
                    if(op.contains("READ")){
                        if(!checkDirty(result)){ dirtyList.add(result);}
                    }

                    if(op.contains("POP") || op.contains("PUSH")){
                        System.out.println(getTinyOpcode(op) + " " + reg);
                        if(op.equals("POP")){ if(!checkDirty(result)){ dirtyList.add(result);}}
                    }
                    else {
                        System.out.println(";sys " + map);
                        System.out.println("sys " + getTinyOpcode(op) + " " + reg);
                    }

                    if(!liveness.contains(result)) {
                        if(!result.equals("")){
                            regFree(result, liveness);
                        }
                    }
                }
                else {

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
        boolean isFirst = true;
        // ensure(): $L1 has register r2
        // move r2 $8
        //if(op1.startswith("$L")
        if (result.equals("$R")) {
            reg1 = regEnsure(c,op1, liveness,isFirst);
            System.out.println("move " + reg1 + " " + getTinyTransform(result));
            checkLive(op1, liveness);
        } else {
            dbgPrint(";" + c + " "+liveness);
            isFirst = true;
            reg1 = regEnsure(c, op1, liveness, isFirst);
            isFirst = false;
            reg2 = regEnsure(c, result, liveness, isFirst);
            if(!checkDirty(result)){ dirtyList.add(result);}
            System.out.println("move " + reg1 + " " + reg2);
            //System.out.println("move " + getTinyTransform(op1) + " " + getTinyTransform(result));
            checkRegLive(liveness);
        }
    }

    public void handleThreeAddress(Code c) {
        String op1 = c.getOp1();
        String op2 = c.getOp2();
        String op = c.getOpcode();
        String type = c.getType();
        String result = c.getResult();
        Set<String> liveness = c.getOut();
        boolean isFirst = false;
        String reg1 = "";
        String reg2 = "";
        String[] operationList = {"ADDI","ADDF","SUBI","SUBF","MULTI","MULTF","DIVI","DIVF"};
        if (Arrays.asList(operationList).contains(op)){
            dbgPrint(";"+c +" " + liveness);
            reg1 = regEnsure(c,op1,liveness, isFirst);
            reg2 = regEnsure(c,op2,liveness, isFirst);
            switchReg(op1, result);
            System.out.println(getTinyOpcode(op) + " " + reg2+" " + reg1);
            checkRegLive(liveness);
        } else {
            dbgPrint(";"+c +" " + liveness);
            reg1 = regEnsure(c, op1, liveness, isFirst);
            reg2 = regEnsure(c, op2, liveness, isFirst);
            if(type.equals("INT")){
                System.out.println("cmpi " + reg1 + " " + reg2 );
            }else if (type.equals("FLOAT")){
                System.out.println("cmpr" + reg1 + " " +reg2);
            }
            checkRegLive(liveness);

            // initialize map
            dbgPrint(";Spilling registers at the end of the Basic Block");
            spillAllReg();
            System.out.println("j" + op.toLowerCase() + " " + getTinyTransform(result));
        }
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
