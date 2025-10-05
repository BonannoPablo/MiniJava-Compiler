package compiler.symboltable;

import compiler.token.Token;

import java.util.List;

public abstract class ClassOrInterfaceEntry {
    protected Token modifier;
    List<AttributeEntry> attributes;

    public void setModifier(Token modifier) {
        this.modifier = modifier;
    }

    public abstract MethodOrConstructor getCurrentMethod();

    public void addAttribute(AttributeEntry attribute){
        attributes.add(attribute);
    }
}
