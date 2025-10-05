package compiler.symboltable;

import java.util.List;

public abstract class MethodOrConstructor {
    List<ParameterEntry> parameters;

    public void setParameters(List<ParameterEntry> parameters) {
        this.parameters = parameters;
    }
}
