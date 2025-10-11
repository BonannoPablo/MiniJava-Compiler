package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;

import java.util.*;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;
import static compiler.token.Token.TokenType.FINAL_WORD;
import static compiler.token.Token.TokenType.STATIC_WORD;

public class InterfaceEntry extends TopLevelEntry {
    private String name;
    private Token token;
    private Token genericType;
    private Token parent;

    public InterfaceEntry(Token token){
        this.token = token;
        name = token.getLexeme();
        methods = new HashMap<>();
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

    @Override
    public void consolidate() throws SemanticException {
        if(consolidated)
            return;
        if(parent != null) {
            InterfaceEntry parentInterface = symbolTable.getInterfaceEntry(parent);
            parentInterface.consolidate();
            for(MethodEntry method : parentInterface.getMethods()){
                Token.TokenType methodModifier = method.getModifier() == null ? null : method.getModifier().getTokenType();
                String methodArityAndName = method.getParameters().size() +  method.getName();
                MethodEntry oldMethod = null;
                if(methodModifier != STATIC_WORD)
                    oldMethod = methods.put(methodArityAndName, method);
                if(oldMethod != null) {
                    String oldMethodNameAndArity = oldMethod.getParameters().size() + oldMethod.getName();
                    methods.put(oldMethodNameAndArity, oldMethod);
                }
            }
        }
    }

    public Iterable<MethodEntry> getMethods() {
        return methods.values();
    }

    public void addMethod(MethodEntry method) throws SemanticException {
        Token.TokenType methodModifier = method.getModifier() == null ? null : method.getModifier().getTokenType();
        if(methodModifier == FINAL_WORD)
            throw new SemanticException("Final methods are not allowed in interfaces", method.getToken());
        String methodArityAndName = method.getParameters().size() + method.getName();
        if(methods.put(methodArityAndName, method) != null)
            throw new SemanticException("Duplicate method name", method.getToken());
        currentMethod = method;
    }

    public boolean checkCircularInheritance(InterfaceEntry classEntry) {
        if(parent == null)
            return false;
        if(name.equals(classEntry.name))
            return true;
        InterfaceEntry parentInterface = symbolTable.getInterfaceEntry(parent);
        return parentInterface.checkCircularInheritance(classEntry);
    }

    public Token getToken() {
        return token;
    }

    public void setCurrentMethod(MethodEntry methodEntry) {
        currentMethod = methodEntry;
    }

    public void checkDeclaration() throws SemanticException {
        checkModifier();
        if(parent != null) {
            InterfaceEntry parentInterface = symbolTable.getInterfaceEntry(parent);
            if(parentInterface != null) {
                if(parentInterface.checkCircularInheritance(this))
                    throw new SemanticException("Illegal cyclic inheritance", token);
                Token.TokenType parentModifier = parentInterface.getModifier();
                if(parentModifier == FINAL_WORD)
                    throw new SemanticException("Cannot inherit from final class", token);
            }
            else
                throw new SemanticException("Parent interface not found", parent);
        }

        for(MethodEntry method : methods.values()){
            method.checkDeclaration();
        }

        for(AttributeEntry attribute : attributes.values()){
            attribute.checkDeclaration();
        }
    }

    private void checkModifier() throws SemanticException {
        if(modifier == STATIC_WORD)
            throw new SemanticException("Static interfaces are not allowed", token);
    }

}
