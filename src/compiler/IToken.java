package compiler;

public interface IToken {
    public static enum TokenType {
        METVARID, CLASSID, INTLITERAL, CHARLITERAL, STRINGLITERAL, EOF
    }
    public String getTokenValue();
    public TokenType getTokenType();
    public int getLineNumber();
}
