package compiler.symboltable;

import compiler.token.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassEntry extends ClassOrInterfaceEntry{
    String name;
    Token token;
    MethodEntry currentMethod;
    Token implementedInterface;
    Token parent;
    Map<String, MethodEntry> methods;
    Map<String, MethodEntry> constructors;
    Token genericType;

    public ClassEntry(Token token){
        this.token = token;
        name = token.getLexeme();
        methods = new HashMap<>();
        constructors = new HashMap<>();
        attributes = new HashMap<>();
    }

    public String getName(){
        return name;
    }

    public void setImplementedInterface(Token implementedInterface){
        this.implementedInterface = implementedInterface;
    }



    public void addMethod(MethodEntry method){
        methods.put(method.getName(), method);
        currentMethod = method;
    }
    public MethodEntry getCurrentMethod() {
        return currentMethod;
    }
    public void addConstructor(MethodEntry constructor){
        constructors.put(constructor.getName(), constructor);
        currentMethod = constructor;
    }

    public void setParent(Token parent){
        this.parent = parent;
    }


    public void addGenericType(Token genericType) {
        this.genericType = genericType;
    }
}
