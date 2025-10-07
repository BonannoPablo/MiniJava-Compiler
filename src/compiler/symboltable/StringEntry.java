package compiler.symboltable;

import compiler.token.Token;
import compiler.token.TokenImpl;

public class StringEntry extends ClassEntry{
    public StringEntry() {
        super(new TokenImpl(Token.TokenType.CLASSID, "String", -1));
    }
    //TODO implement String methods entries
}
