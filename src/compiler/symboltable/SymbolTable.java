package compiler.symboltable;

import java.util.Hashtable;

public class SymbolTable {
    Hashtable<String, ClassTable> classTable;
    Hashtable<String, InterfaceTable> interfaceTable;
    public SymbolTable() {
        classTable = new Hashtable<>();
        interfaceTable = new Hashtable<>();
    }

    public void putClass(ClassTable classItem){
        classTable.put(classItem.getName(), classItem);

    }



}
