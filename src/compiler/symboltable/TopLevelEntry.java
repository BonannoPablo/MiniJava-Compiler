package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;

import java.util.Map;

public abstract class TopLevelEntry {
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


}