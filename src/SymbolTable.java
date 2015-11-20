import java.util.*;

/**
 * Created by hgp on 10/2/2015.
 */
public interface SymbolTable {
    String getScope();
    ArrayList<SymbolEntry> getDecls();
    ArrayList<SymbolTable> getChildren();
    List<Code> getCodeList();
    List<Integer> getOffsetList();
    void setScope(String scope);
    void addOffset(int offset);
    void addElement(SymbolEntry e);
    void addChild(SymbolTable func);
    void printTable();
    void printTiny();
    SymbolTable getParent();
    String lookUpType(String name);
    void addParamEntry(String name, String type);
    String lookUpVar(String name);
    void addDeclEntry(String name, String type);
    void addCode(Code c);
    void addFirst(Code c);
    void addOneAddressCode(String opcode, String result, String type);
    void addOneAddressCode(String opcode, String result, String type, boolean getNext);
    void addTwoAddressCode(String opcode, String op1, String type);
    void addThreeAddressCode(String opcode, String op1, String op2, String type);
    void addResultAddressCode(String opcode, String op1,String type);
}
