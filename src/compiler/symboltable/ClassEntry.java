package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;
import compiler.token.TokenImpl;

import java.util.HashMap;
import java.util.Map;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;
import static compiler.token.Token.TokenType.*;

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
        parent = new TokenImpl(Token.TokenType.CLASSID, "Object", -1);
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


    public void checkDeclaration() throws SemanticException {
        if(parent != null) {
            ClassEntry parentClass = symbolTable.existsClass(parent);
            if(parentClass != null) {
                parentClass.checkCircularInheritance(this);
                Token.TokenType parentModifier = parentClass.getModifier();
                checkAbstractInheritance(parentModifier);
                if(parentModifier == FINAL_WORD || parentModifier == STATIC_WORD)
                    throw new SemanticException("Cannot inherit from final class");
            }
            else
                throw new SemanticException("Parent class not found");

        } else if(implementedInterface != null){
            InterfaceEntry interfaceObject = symbolTable.existsInterface(implementedInterface);
            if(interfaceObject != null)
                interfaceObject.checkCircularInheritance(this);
            else
                throw new SemanticException("Interface not found");
        }

        for(MethodEntry method : methods.values()){
            method.checkDeclaration();
        }

        if(!constructors.isEmpty() && modifier == ABSTRACT_WORD)
            throw new SemanticException("Cannot have abstract constructors");
        for(MethodEntry constructor : constructors.values()){
            constructor.checkDeclaration();
        }
        for(AttributeEntry attribute : attributes.values()){
            attribute.checkDeclaration();
        }
    }

    private void checkAbstractInheritance(Token.TokenType parentModifier) throws SemanticException {
        Token.TokenType modifierTokenType;
        if(modifier != null)
            modifierTokenType = modifier;
        else
            modifierTokenType = null;

        if(parentModifier != ABSTRACT_WORD && modifierTokenType == ABSTRACT_WORD)
            throw new SemanticException("Abstract class cannot inherit from concrete class");
    }

    private void checkCircularInheritance(ClassEntry classEntry) {
        //TODO
    }
}
