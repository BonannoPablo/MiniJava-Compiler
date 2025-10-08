package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;

import java.util.List;
import java.util.Map;

public abstract class ClassOrInterfaceEntry {
    protected Token modifierToken;
    protected Token.TokenType modifier;
    Map<String, AttributeEntry> attributes;

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
            throw new SemanticException("Duplicate attribute name");

    }

    protected Token.TokenType getModifier() {
        return modifier;
    }
}