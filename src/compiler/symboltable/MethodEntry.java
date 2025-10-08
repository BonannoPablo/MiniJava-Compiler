package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;

import java.util.List;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

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

    public void checkDeclaration() {
    }
    public void checkConstructionDeclaration() throws SemanticException {
        if(!name.equals(symbolTable.getCurrentClass().getName()))
            throw new SemanticException("Constructor should be named after class name");
    }
}