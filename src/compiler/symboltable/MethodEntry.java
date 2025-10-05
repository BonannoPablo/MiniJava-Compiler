package compiler.symboltable;

import compiler.token.Token;

import java.util.List;

public class MethodEntry extends MethodOrConstructor{
    String name;
    Token token;
    String returnType;
    Token modifier;
    Token visibility;

    public MethodEntry(Token token){
        this.token = token;
        name = token.getLexeme();
    }

    public void setReturnType(String type) {
        returnType = type;
    }

    public void setModifier(Token modifier) {
        this.modifier = modifier;
    }

    public void setVisibility(Token visibility) {
        this.visibility = visibility;
    }


}