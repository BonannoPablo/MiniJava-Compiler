package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;


import java.util.Hashtable;

import java.util.Map;

public class SymbolTable {
    Map<String, ClassEntry> classTable;
    Map<String, InterfaceEntry> interfaceTable;
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
            //this exception shouldn't be reachable
        }
    }

    public ClassOrInterfaceEntry getCurrentClassOrInterface() {
        if(currentClass != null)
            return currentClass;
        else if(currentInterface != null)
            return currentInterface;
        else
            return null;
    }

    public ClassEntry getCurrentClass() {
        return currentClass;
    }

    public void putClass(ClassEntry classItem) throws SemanticException {
        if(classTable.put(classItem.getName(), classItem) != null){
            throw new SemanticException("Duplicate class");
        }
        currentClass = classItem;
        currentInterface = null;
    }

    public void putInterface(InterfaceEntry interfaceItem) throws SemanticException {
        if(interfaceTable.put(interfaceItem.getName(), interfaceItem) != null){
            throw new SemanticException("Duplicate interface");
        }

        currentInterface = interfaceItem;
        currentClass = null;
    }


    public InterfaceEntry getCurrentInterface() {
        return currentInterface;
    }

    public void print() {
        for(ClassEntry c: classTable.values()){
            System.out.println(c.toString());
        }
        for(InterfaceEntry i: interfaceTable.values()){
            System.out.println(i.toString());
        }
    }

    public void checkDeclarations() throws SemanticException {
        for(ClassEntry c: classTable.values()){
            currentClass = c;
            c.checkDeclaration();
        }
    }

    public ClassEntry existsClass(Token parent) {
        return classTable.get(parent.getLexeme());
    }

    public InterfaceEntry existsInterface(Token parent) {
        return interfaceTable.get(parent.getLexeme());
    }
}
