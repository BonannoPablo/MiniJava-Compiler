package compiler.symboltable.types;

import compiler.token.Token;

public abstract class Type {
    String name;
    Token token;

    public Type(Token token) {
        this.name = token.getLexeme();
        this.token = token;
    }

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Token getToken() {
        return token;
    }

    public String getGenericType(){
        return "";
    }

    public abstract Token getGenericTypeToken() ;

    public abstract boolean conforms(Type t);
}
