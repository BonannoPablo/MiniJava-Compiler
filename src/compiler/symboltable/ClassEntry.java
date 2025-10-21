package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;
import compiler.token.TokenImpl;

import java.util.HashMap;
import java.util.Map;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;
import static compiler.token.Token.TokenType.*;

public class ClassEntry extends TopLevelEntry {
    private Token token;
    private Token implementedInterface;
    private Token parent;
    private Token parentGenericType;
    private Map<Integer, MethodEntry> constructors;
    private Token genericType;
    private Token[] genericTypeMap;

    public ClassEntry(Token token){
        this.token = token;
        name = token.getLexeme();
        methods = new HashMap<>();
        constructors = new HashMap<>();
        attributes = new HashMap<>();
        parent = new TokenImpl(Token.TokenType.CLASSID, "Object", -1);
        parentGenericType = null;
        genericTypeMap = null;
    }

    public void setImplementedInterface(Token implementedInterface){
        this.implementedInterface = implementedInterface;
    }



    public void addMethod(MethodEntry method) throws SemanticException {
        String methodArityAndName = method.getParameters().size() + method.getName();
        if(methods.put(methodArityAndName, method) != null)
            throw new SemanticException("Duplicate method name", method.getToken());
        currentMethod = method;
    }
    public MethodEntry getCurrentMethod() {
        return currentMethod;
    }

    @Override
    public void consolidate() throws SemanticException {
        if(consolidated || name.equals("Object"))
            return;
        ClassEntry parentClass = symbolTable.getClassEntry(parent);
        parentClass.consolidate();
        symbolTable.setCurrentClass(this);
        for(MethodEntry method : parentClass.getMethods()){
            Token.TokenType methodModifier = method.getModifier() == null ? null : method.getModifier().getTokenType();
            String methodArityAndName = method.getParameters().size() + method.getName();
            MethodEntry oldMethod = methods.put(methodArityAndName, method);
            if(oldMethod != null){
                boolean signatureMatch = method.matchSignatures(oldMethod);
                boolean modifierFinalOrStatic = method.getModifier() != null && (method.getModifier().getTokenType() == FINAL_WORD || method.getModifier().getTokenType() == STATIC_WORD);
                if(!signatureMatch)
                    throw new SemanticException("Duplicate method", oldMethod.getToken());
                else if(modifierFinalOrStatic)
                    throw new SemanticException("Cannot override final or static method", oldMethod.getToken());
                else if(oldMethod.getModifier() != null && oldMethod.getModifier().getTokenType() == STATIC_WORD)
                    throw new SemanticException("Static method cannot override non static method", oldMethod.getToken());
                else {
                    String oldMethodArityAndName = oldMethod.getParameters().size() + oldMethod.getName();
                    methods.put(oldMethodArityAndName, oldMethod);
                }
            } else{
                if(methodModifier == ABSTRACT_WORD && modifier != ABSTRACT_WORD)
                    throw new SemanticException("Class is not abstract and does not override abstract method in parent", token);
            }

        }
        for(AttributeEntry attribute : parentClass.getAttributes()){
            AttributeEntry a = attributes.put(attribute.getName(), attribute);
            if(a != null)
                throw new SemanticException("Duplicate attribute", a.getToken());
        }
        consolidated = true;
        checkImplementedMethods();
    }

    private void checkImplementedMethods() throws SemanticException {
        if(implementedInterface != null && modifier != ABSTRACT_WORD){
            InterfaceEntry interfaceObject = symbolTable.getInterface(implementedInterface);
            if(interfaceObject == null)
                throw new SemanticException("Interface not found", implementedInterface);
            interfaceObject.consolidate();
            for(MethodEntry method : interfaceObject.getMethods()){
                String methodToImplementName = method.getParameters().size() + method.getName();
                if(methods.containsKey(methodToImplementName)){
                    MethodEntry methodEntry = methods.get(methodToImplementName);
                    if(! method.matchSignatures(methodEntry)){
                        throw new SemanticException("Class should implement all interface methods", token);
                    }
                } else {
                    throw new SemanticException("Class should implement all interface methods", token);
                }
            }
        }
    }

    public Iterable<AttributeEntry> getAttributes() {
        return attributes.values();
    }

    public Iterable<MethodEntry> getMethods() {
        return methods.values();
    }

    public void addConstructor(MethodEntry constructor) throws SemanticException {
        if(modifier == ABSTRACT_WORD)
            throw new SemanticException("Abstract class cannot have constructors", constructor.getToken());
        if(constructors.put(constructor.getParameters().size(), constructor) != null)
            throw new SemanticException("Constructor already defined", constructor.getToken());
        currentMethod = constructor;
    }

    public void setParent(Token parent) throws SemanticException {
        if(parent.getLexeme().equals(token.getLexeme()))
            throw new SemanticException("Cannot inherit from itself", token);
        this.parent = parent;
    }


    public void addGenericType(Token genericType) {
        this.genericType = genericType;
    }


    public void checkDeclaration() throws SemanticException {
        symbolTable.setCurrentClass(this);
        if(parent != null) {
            ClassEntry parentClass = symbolTable.getClassEntry(parent);
            if(parentClass != null) {
                if(parentClass.checkCircularInheritance(this))
                    throw new SemanticException("Illegal cyclic inheritance", token);
                Token.TokenType parentModifier = parentClass.getModifier();
                if(parentModifier == FINAL_WORD)
                    throw new SemanticException("Cannot inherit from final class", token);
                if(parentModifier == STATIC_WORD)
                    throw new SemanticException("Cannot inherit from static class", token);
                checkAbstractInheritance(parentModifier);
            }
            else
                throw new SemanticException("Parent class not found", parent);

            if(parentGenericType != null && genericType == null) {
                if(!symbolTable.existsClass(parentGenericType.getLexeme()))
                    throw new SemanticException("Cannot find symbol", parentGenericType);
            }else if(parentGenericType != null){
                if(! symbolTable.getClassEntry(parent).hasGenericType())
                    throw new SemanticException("Parent class does not take generic parameter", parentGenericType);
            }else{
                if (symbolTable.getClassEntry(parent).getGenericType() != null)
                    throw new SemanticException("Raw type not allowed. Must specify generic type", parent);
            }


        } else if(implementedInterface != null){
            InterfaceEntry interfaceObject = symbolTable.getInterface(implementedInterface);
            if(interfaceObject == null)
                throw new SemanticException("Interface not found", implementedInterface);
        }

        initializeGenericTypeMap();

        for(MethodEntry method : methods.values()){
            method.checkDeclaration();
        }

        for(MethodEntry constructor : constructors.values()){
            constructor.checkConstructionDeclaration();
        }
        for(AttributeEntry attribute : attributes.values()){
            attribute.checkDeclaration();
        }
    }

    private void initializeGenericTypeMap() {
        if(parentGenericType != null) {
            genericTypeMap = new Token[2];
            genericTypeMap[0] = symbolTable.getClassEntry(parent).getGenericType();
            genericTypeMap[1] = parentGenericType;
        }
    }

    public Token[] getGenericTypeMap() {
        return genericTypeMap;
    }

    public boolean hasGenericType() {
        return genericType != null;
    }

    private void checkAbstractInheritance(Token.TokenType parentModifier) throws SemanticException {
        Token.TokenType modifierTokenType;
        if(modifier != null)
            modifierTokenType = modifier;
        else
            modifierTokenType = null;

        if(parentModifier != ABSTRACT_WORD && modifierTokenType == ABSTRACT_WORD && !parent.getLexeme().equals("Object"))
            throw new SemanticException("Abstract class cannot inherit from concrete class", token);
    }

    private boolean checkCircularInheritance(ClassEntry classEntry){
        if(name.equals("Object"))
            return false;
        if(name.equals(classEntry.name))
            return true;
        ClassEntry parentClass = symbolTable.getClassEntry(parent);
        return parentClass.checkCircularInheritance(classEntry);
    }

    public String print() {
        String interfaceName = implementedInterface == null ? "none" : implementedInterface.getLexeme();
        String genericName = genericType == null ? "none" : genericType.getLexeme();
        StringBuilder s = new StringBuilder(name + "\nGeneric: " + genericName + "\nParent: " + parent.getLexeme() + "\nInterface: " + interfaceName + "\n---Attributes---\n");
        for (AttributeEntry attribute : attributes.values()) {
            s.append(attribute.print()).append("\n");
        }
        s.append("---Methods---\n");
        for (MethodEntry method : methods.values()) {
            s.append(method.print()).append("\n");
        }
        s.append("---Constructors---\n");
        for (MethodEntry constructor : constructors.values()) {
            s.append(constructor.print()).append("\n");
        }
        return s.toString();
    }

    public Token getToken() {
        return token;
    }

    public void setCurrentMethod(MethodEntry methodEntry) {
        currentMethod = methodEntry;
    }

    public void setParentGenericType(Token token) {
        this.parentGenericType = token;
    }

    public Token getGenericType() {
        return genericType;
    }

    public void printAST(){
        System.out.println("AST for class " + name + ":");
        for(MethodEntry method : methods.values()){
            method.printAST();
        }
    }
}
