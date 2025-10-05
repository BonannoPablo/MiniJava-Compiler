package compiler.symboltable;

import compiler.token.Token;

import java.util.List;

public class ConstructorEntry extends MethodOrConstructor{
    Token token;
    List<ParameterEntry> parameters;

    public ConstructorEntry(Token token) {
        this.token = token;
    }

    public void setParameters(List<ParameterEntry> parameters) {
        this.parameters = parameters;
    }

    public List<ParameterEntry> getParameters() {
        return parameters;
    }
}
