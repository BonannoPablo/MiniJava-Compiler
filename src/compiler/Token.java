package compiler;

public class Token implements IToken {

    private String tokenValue;
    private TokenType tokenType;
    private int lineNumber;

    public Token(String tokenValue, TokenType tokenType, int lineNumber){
        this.tokenValue = tokenValue;
        this.tokenType = tokenType;
        this.lineNumber = lineNumber;
    }

    @Override
    public String getTokenValue() {
        return tokenValue;
    }

    @Override
    public TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }
}
