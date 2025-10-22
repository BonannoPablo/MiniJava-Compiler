package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.symboltable.types.Type;
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
        Token genericType = symbolTable.getCurrentClass().getGenericType();
        boolean genericTypeMatch = genericType != null && type.getToken().getLexeme().equals(genericType.getLexeme());
        if(type.getToken().getTokenType() == Token.TokenType.CLASSID
        && ! (symbolTable.existsClass(type.getName()) || genericTypeMatch))
                throw new SemanticException("Class does not exist", type.getToken()); //TODO change msg

        //TODO check generic type if parameters???
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
