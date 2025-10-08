package compiler.symboltable;

import compiler.token.Token;

import java.util.List;

public class MethodEntry{
    String name;
    Token token;
    Type returnType;
    Token modifier;
    Token visibility;
    List<ParameterEntry> parameters;

    public MethodEntry(Token token){
        this.token = token;
        name = token.getLexeme();
    }

    public void setReturnType(Type type) {
        returnType = type;
    }

    public void setModifier(Token modifier) {
        this.modifier = modifier;
    }

    public void setVisibility(Token visibility) {
        this.visibility = visibility;
    }


    public String getName() {
        return name;
    }

    public void setParameters(List<ParameterEntry> parameterEntries) {
        parameters = parameterEntries;
    }
}