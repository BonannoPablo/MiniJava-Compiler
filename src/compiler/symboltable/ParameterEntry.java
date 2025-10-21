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
        Token containerClassGenericType = symbolTable.getCurrentClass().getGenericType();
        boolean genericTypeMatch = containerClassGenericType != null && type.getToken().getLexeme().equals(containerClassGenericType.getLexeme());
        boolean existsTypeClass = symbolTable.existsClass(type.getName());

        if(type.getToken().getTokenType() == Token.TokenType.CLASSID
        && ! (existsTypeClass || genericTypeMatch))
                throw new SemanticException("Class does not exist", type.getToken()); //TODO change msg

        boolean declaredClassHasGenericType = existsTypeClass && symbolTable.getClassEntry(type.getName()).getGenericType() != null;

        if(!type.getGenericType().isEmpty() &&
           ! (symbolTable.existsClass(type.getGenericType()) || (containerClassGenericType != null && type.getGenericType().equals(containerClassGenericType.getLexeme()))))
            throw new SemanticException("Class does not exist", type.getGenericTypeToken());



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
