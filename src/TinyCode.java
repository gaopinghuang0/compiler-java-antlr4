
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
                System.out.println(";Spilling variable: " + value);
                System.out.println("move " + key + " " +getTinyTransform(value));
                map.put(key, "null");
            }
        }
    }

    public void initMap(){
        for (int i=0; i<4; i++){
            this.map.put("r"+i, "null");
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
    }

    public String regEnsure(Code c, String op, Set<String> liveness) {
        // check if opr already has regiser
        if (!op.startsWith("$")) return op;
        String reg = "";
        for (String key : map.keySet()) {
            String value = map.get(key);
            // reg found
            if (value.equals(op)) {
                reg = key;
                System.out.println(";ensure(): " + op + " has register " + reg + " ");

            }
        }
        // reg not found, allocate a reg for op
        if (reg.isEmpty()) {
            reg = regAllocate(c, op, liveness);
            //|| c.getOp1().equals(op)
            // TODO: handle op1 in STORE case, "loading"
            if (c.getClass() == ThreeAddressCode.class || c.getOpcode().contains("WRITE")) {

                System.out.println(";loading " + op + " to register " + reg);
                System.out.println("move " + getTinyTransform(op) + " " + reg);
            } else if (c.getOpcode().contains("STORE")) {
                if (c.getOp1().contains("$")) {
                    System.out.println(";loading " + op + " to register " + reg);
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
                System.out.println(";ensure(): " + op + " gets register "+reg+ " " + this.map);
                return reg;
            }

        }

        // select reg that is not in liveness to spill
        for(String key : map.keySet()){
            String value = map.get(key);
            ArrayList<String> opList = c.getOpArray();
            //System.out.println(opList);
            if ((!liveness.contains(value)) && (!opList.contains(value))) {
                System.out.println("variable not live: " + value);
                System.out.println(";allocate() has to spill " + value);
                System.out.println(";Spilling variable: " + value);
                System.out.println("move " + key + " " + getTinyTransform(value));
                System.out.println(";ensure(): " + op + " gets register " + reg + " " + this.map);
                this.map.put(key, op);
                dirtyList.remove(value);
                reg = key;
                break;
            } else {
                // randomly pick one variable to spill, but not op
                if (!opList.contains(value)) {
                    System.out.println(";allocate() has to spill " + value);
                    this.map.put(key, "null");
                    if (dirtyList.contains(value)) {
                        System.out.println(";Spilling variable: " + value);
                        System.out.println("move " + key + " " + getTinyTransform(value));
                        dirtyList.remove(value);

                    }
                    reg = regEnsure(c, op, liveness);
                    break;
                }
            }
        }

        return reg;
    }

    public void regFree(String op, Set<String> liveness){
        // if r is marked dirty and live
        // generate store
        // mark r as free
        System.out.println(";Freeing unused variable " + op);
        String key = regLookup(op);
        this.map.put(key,"null");
        if (this.dirtyList.contains(op)) {
            System.out.println(";Spilling variable: " + op);
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
        System.out.println(";Switching owner of register " + reg + " to " + result);
        dirtyList.add(result);
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
                System.out.println(";Spilling registers at the end of the Basic Block" + map);
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
                System.out.println("unlnk");
                System.out.println(getTinyOpcode(c.getOpcode()));
                break;
            case "LINK":
                //System.out.println(op.toLowerCase() + " " + (declId));
                int resultValue = localTemp + declId -2;
                System.out.println(op.toLowerCase() + " " + resultValue);

                break;
            default:  // READ or WRITE

                if(op.contains("READ") || op.contains("WRITEF") || op.contains("POP") || op.contains("PUSH")){
                    reg = regEnsure(c, result, liveness);
                    if(op.contains("READF")){
                        dirtyList.add(result);
                    }

                    if(op.contains("POP") || op.contains("PUSH")){
                        System.out.println(getTinyOpcode(op) + " " + reg);
                        if(op.equals("POP")){ dirtyList.add(result);}
                    }
                    else {
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
        // ensure(): $L1 has register r2
        // move r2 $8
        //if(op1.startswith("$L")
        if (result.equals("$R")) {
            reg1 = regEnsure(c,op1, liveness);
//            System.out.println("move " + getTinyTransform(op1) + " "+reg1);

            System.out.println("move " + reg1 + " " + getTinyTransform(result));
            checkLive(op1, liveness);
        } else {
            System.out.println(";" + c + " "+liveness);

            reg1 = regEnsure(c, op1, liveness);
            reg2 = regEnsure(c, result, liveness);

            this.dirtyList.add(result);
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
        String reg1 = "";
        String reg2 = "";
        String[] operationList = {"ADDI","ADDF","SUBI","SUBF","MULTI","MULTF","DIVI","DIVF"};
        if (Arrays.asList(operationList).contains(op)){
            //System.out.println("move " + getTinyTransform(op1) +" "+ getTinyTransform(result));
            //System.out.println(getTinyOpcode(op)+" " + getTinyTransform(op2)+ " " + getTinyTransform(result));
            reg1 = regEnsure(c,op1,liveness);
            reg2 = regEnsure(c,op2,liveness);
            switchReg(op1, result);
            System.out.println(getTinyOpcode(op) + " " + reg2+" " + reg1);
            checkRegLive(liveness);
        } else {
            reg1 = regEnsure(c, op1, liveness);
            reg2 = regEnsure(c, op2, liveness);
            if(type.equals("INT")){
                System.out.println("cmpi " + reg1 + " " + reg2 );
            }else if (type.equals("FLOAT")){
                System.out.println("cmpr" + reg1 + " " +reg2);
            }
            checkRegLive(liveness);

            // initialize map
            System.out.println(";Spilling registers at the end of the Basic Block");
            spillAllReg();
            System.out.println("j" + op.toLowerCase() + " " +getTinyTransform(result));
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
}
