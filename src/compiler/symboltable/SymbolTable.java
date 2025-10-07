package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;
import compiler.token.TokenImpl;

import java.util.Hashtable;
import java.util.Iterator;

public class SymbolTable {
    Hashtable<String, ClassEntry> classTable;
    Hashtable<String, InterfaceEntry> interfaceTable;
    ClassEntry currentClass;
    InterfaceEntry currentInterface;

    public SymbolTable() {
        classTable = new Hashtable<>();
        interfaceTable = new Hashtable<>();
        generateDefaultClasses();
    }

    private void generateDefaultClasses() {
        try {
            putClass(new ObjectEntry());
            putClass(new StringEntry());
            putClass(new SystemEntry());
        } catch(SemanticException e){
            //
        }
    }

    public ClassOrInterfaceEntry getCurrentClassOrInterface() {
        if(currentClass != null)
            return currentClass;
        else
            return currentInterface;
    }

    public ClassEntry getCurrentClass() {
        return currentClass;
    }

    public void putClass(ClassEntry classItem) throws SemanticException {
        if(classTable.put(classItem.getName(), classItem) == null){
            throw new SemanticException("Duplicate class");
        }
        currentClass = classItem;
        currentInterface = null;
    }

    public void putInterface(InterfaceEntry interfaceItem){
        interfaceTable.put(interfaceItem.getName(), interfaceItem);
        currentInterface = interfaceItem;
        currentClass = null;
    }


    public InterfaceEntry getCurrentInterface() {
        return currentInterface;
    }

    public void print() {
        System.out.println("Class Table:");
        for (Iterator<String> it = classTable.keys().asIterator(); it.hasNext(); ) {
            String classEntry = it.next();
            System.out.println("\t"+ classEntry);
        }

        System.out.println("\n\nInterface Table:");
        for (Iterator<String> it = interfaceTable.keys().asIterator(); it.hasNext(); ) {
            String classEntry = it.next();
            System.out.println("\t"+ classEntry);
        }

    }
}
