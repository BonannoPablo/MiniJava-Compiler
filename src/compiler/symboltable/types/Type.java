package compiler.symboltable.types;

import compiler.token.Token;

public abstract class Type {
    String name;
    Token token;

    public Type(Token token) {
        this.name = token.getLexeme();
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public Token getToken() {
        return token;
    }
}
