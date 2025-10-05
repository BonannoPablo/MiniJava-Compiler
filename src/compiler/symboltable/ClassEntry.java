package compiler.symboltable;

import compiler.token.Token;

import java.util.List;

public class ClassEntry extends ClassOrInterfaceEntry{
    String name;
    Token token;
    MethodOrConstructor currentMethod;
    Token implementedInterface;
    Token parent;
    List<MethodEntry> methods;
    List<ConstructorEntry> constructors;
    Token genericType;

    public ClassEntry(Token token){
        this.token = token;
        name = token.getLexeme();
        methods = new java.util.LinkedList<>();
        constructors = new java.util.LinkedList<>();
        attributes = new java.util.LinkedList<>();
    }

    public String getName(){
        return name;
    }

    public void setImplementedInterface(Token implementedInterface){
        this.implementedInterface = implementedInterface;
    }



    public void addMethod(MethodEntry method){
        methods.add(method);
        currentMethod = method;
    }
    public MethodOrConstructor getCurrentMethod() {
        return currentMethod;
    }
    public void addConstructor(ConstructorEntry constructor){
        constructors.add(constructor);
        currentMethod = constructor;
    }

    public void setParent(Token parent){
        this.parent = parent;
    }


    public void addGenericType(Token genericType) {
        this.genericType = genericType;
    }
}
