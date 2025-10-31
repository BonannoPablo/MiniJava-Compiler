package compiler.symboltable;

import compiler.ast.expressions.ExpressionNode;
import compiler.exceptions.SemanticException;
import compiler.token.Token;

import java.util.Map;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public abstract class TopLevelEntry {
    protected String name;
    protected Token modifierToken;
    protected Token.TokenType modifier;
    protected Map<String, AttributeEntry> attributes;
    protected boolean consolidated = false;
    protected Map<String, MethodEntry> methods;
    protected MethodEntry currentMethod;

    public void setModifier(Token modifierToken) {
        this.modifierToken = modifierToken;
        if(modifierToken == null)
            this.modifier = null;
        else
            this.modifier = modifierToken.getTokenType();
    }

    public String getName(){
        return name;
    }

    public abstract MethodEntry getCurrentMethod();

    public void addAttribute(AttributeEntry attribute) throws SemanticException {
        AttributeEntry attributeEntry = attributes.put(attribute.getName(), attribute);
        if(attributeEntry != null)
            throw new SemanticException("Duplicate attribute name", attribute.getToken());
    }

    protected Token.TokenType getModifier() {
        return modifier;
    }


    public abstract void consolidate() throws SemanticException;

    public MethodEntry getMethod(String arityAndName) {
        return methods.get(arityAndName);
    }


    public boolean hasAttribute(String name) {
        var currentClass = symbolTable.getCurrentClass();
        while(! attributes.containsKey(currentClass.getName()+name) && ! currentClass.getName().equals("Object")){
            currentClass = symbolTable.getClassEntry(currentClass.getParent());
        }
        return attributes.containsKey(currentClass.getName()+name);
    }

    public AttributeEntry getAttribute(String name) {
        var currentClass = symbolTable.getCurrentClass();
        while(! attributes.containsKey(currentClass.getName()+name) && ! currentClass.getName().equals("Object")){
            currentClass = symbolTable.getClassEntry(currentClass.getParent());
        }
        return attributes.get(currentClass.getName()+name);
    }
}