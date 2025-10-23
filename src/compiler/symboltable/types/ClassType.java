package compiler.symboltable.types;

import compiler.token.Token;

public class ClassType extends Type{
    Token genericType;

    public ClassType(Token token, Token genericType) {
        super(token);
        this.genericType = genericType;
    }

    public String getGenericType() {
        return genericType != null ? genericType.getLexeme() : "";
    }

    @Override
    public Token getGenericTypeToken() {
        return genericType;
    }
}
