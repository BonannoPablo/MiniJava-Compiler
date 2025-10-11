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
        ClassEntry classEntry = classTable.put(classItem.getName(), classItem);
        if(classEntry != null){
            throw new SemanticException("Duplicate class", classItem.getToken());
        }
        currentClass = classItem;
        currentInterface = null;
    }

    public void putInterface(InterfaceEntry interfaceItem) throws SemanticException {
        if(interfaceTable.put(interfaceItem.getName(), interfaceItem) != null){
            throw new SemanticException("Duplicate interface", interfaceItem.getToken());
        }

        currentInterface = interfaceItem;
        currentClass = null;
    }


    public InterfaceEntry getCurrentInterface() {
        return currentInterface;
    }

    public void print() {
        for(ClassEntry c: classTable.values()){
            System.out.println(c.print());
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

        for(InterfaceEntry i: interfaceTable.values()){
            currentInterface = i;
            i.checkDeclaration();
        }
    }

    public ClassEntry getClassEntry(Token parent) {
        return classTable.get(parent.getLexeme());
    }

    public InterfaceEntry getInterface(Token parent) {
        return interfaceTable.get(parent.getLexeme());
    }

    public boolean existsClass(String name) {
        return classTable.containsKey(name);
    }

    public void consolidate() throws SemanticException {
        for(ClassEntry c: classTable.values()){
            c.consolidate();
        }
        for(InterfaceEntry i: interfaceTable.values()){
            i.consolidate();
        }
    }

    public InterfaceEntry getInterfaceEntry(Token parent) {
        return interfaceTable.get(parent.getLexeme());
    }

    public void setCurrentClass(ClassEntry classEntry) {
        currentClass = classEntry;
    }
}
