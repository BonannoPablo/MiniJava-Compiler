package compiler.token;

public class TokenImpl implements Token {

    private TokenType tokenType;
    private String lexeme;
    private int lineNumber;

    public TokenImpl(TokenType tokenType, String lexeme, int lineNumber){
        this.lexeme = lexeme;
        this.tokenType = tokenType;
        this.lineNumber = lineNumber;
    }

    @Override
    public TokenType getTokenType() {
        return tokenType;
    }

    @Override
    public String getLexeme() {
        return lexeme;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }
}
