package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;

import java.util.List;
import java.util.Map;

public abstract class ClassOrInterfaceEntry {
    protected Token modifier;
    Map<String, AttributeEntry> attributes;

    public void setModifier(Token modifier) {
        this.modifier = modifier;
    }

    public abstract MethodEntry getCurrentMethod();

    public void addAttribute(AttributeEntry attribute) throws SemanticException {
        if(attributes.put(attribute.getName(), attribute) != null)
            throw new SemanticException("Duplicate attribute name");
    }
}