package compiler.exceptions;

import compiler.token.IToken;

public class SyntacticException extends Exception {
    private IToken tokenFound;
    //private final IToken.TokenType TokenExpected;
    public SyntacticException(IToken tokenFound, String tokenTypeExpected) {
        super("Syntax error in line: " + tokenFound.getLineNumber() + ". Expected: " + tokenTypeExpected + " but found: " + tokenFound.getLexeme());
        this.tokenFound = tokenFound;
    }
    public int getLineNumber(){
        return tokenFound.getLineNumber();
    }
    public String getLexeme(){
        return tokenFound.getLexeme();
    }

}
