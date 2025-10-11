package compiler.exceptions;

import compiler.token.Token;

public class SemanticException extends Exception{
    private Token token;

    public SemanticException(String message, Token token) {
        super(message);
        this.token = token;
    }

    public Token getToken(){
        return token;
    }
}
