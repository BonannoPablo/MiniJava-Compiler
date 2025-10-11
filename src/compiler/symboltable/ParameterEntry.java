package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class ParameterEntry {
    Token token;
    String name;
    Type type;

    public ParameterEntry(Type type, Token token) {
        this.type = type;
        this.token = token;
        name = token.getLexeme();
    }

    public void checkDeclaration() throws SemanticException {
        if(type.getToken().getTokenType() == Token.TokenType.CLASSID
        && ! symbolTable.existsClass(type.getName()))
            throw new SemanticException("Class does not exist", type.getToken()); //TODO change msg
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Token getToken() {
        return token;
    }
}
