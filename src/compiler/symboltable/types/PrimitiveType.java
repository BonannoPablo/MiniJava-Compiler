package compiler.symboltable.types;

import compiler.token.Token;

public class PrimitiveType extends Type{
    public PrimitiveType(Token token) {
        super(token);
    }

    public PrimitiveType(String name) {
        super(name);
    }

    @Override
    public Token getGenericTypeToken() {
        return null;
    }

}
