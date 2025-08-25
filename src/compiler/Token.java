package compiler;

public class Token implements IToken{
    String tokenValue;

    public Token(String tokenValue){
        this.tokenValue = tokenValue;
    }

    @Override
    public String getTokenValue() {
        return tokenValue;
    }
}
