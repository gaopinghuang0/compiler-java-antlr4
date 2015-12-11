import java.util.*;

/**
 * Created by hgp on 10/2/2015.
 */
public interface SymbolTable {
    String getScope();
    ArrayList<SymbolEntry> getDecls();
    ArrayList<SymbolTable> getChildren();
    List<Code> getCodeList();
    void setScope(String scope);
    int getParamId();
    int getDeclId();
    int getLocalTemp();
    void addElement(SymbolEntry e);
    void addChild(SymbolTable func);
    void appendToCodeList(List<Code> newCodeList);
    void saveAndResetId();
    void printTable();
    void printIR();
    void printCodeOut();
    void doLivenessAnalysis(List<String> globalTemp);
    void printTiny(int paramId, int localTemp, int declId);
    SymbolTable getParent();
    String lookUpType(String name);
    void addParamEntry(String name, String type);
    String lookUpVar(String name);
    void addDeclEntry(String name, String type);
    void addCode(Code c);
    Code addOneAddressCode(String opcode, String result, String type);
    Code addOneAddressCode(String opcode, String result, String type, boolean getNext);
    Code addTwoAddressCode(String opcode, String op1, String type);
    Code addThreeAddressCode(String opcode, String op1, String op2, String type);
    Code addResultAddressCode(String opcode, String op1,String type);
}
