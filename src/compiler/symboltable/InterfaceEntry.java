package compiler.symboltable;

import compiler.token.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InterfaceEntry extends ClassOrInterfaceEntry{
    private String name;
    private Token token;
    private Token genericType;
    private Token parent;
    private MethodEntry currentMethod;
    private List<MethodEntry> methods;

    public InterfaceEntry(Token token){
        this.token = token;
        name = token.getLexeme();
        methods = new LinkedList<>();
        attributes = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void addGenericType(Token token) {
    }

    public void setParent(Token interfaceClass) {
        parent = interfaceClass;
    }

    public MethodEntry getCurrentMethod() {
        return currentMethod;
    }

    public void addMethod(MethodEntry method){
        methods.add(method);
        currentMethod = method;
    }
}
