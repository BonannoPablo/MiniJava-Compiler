package compiler.symboltable;

import compiler.token.Token;
import compiler.token.TokenImpl;

public class SystemEntry extends ClassEntry{

    public SystemEntry() {
        super(new TokenImpl(Token.TokenType.CLASSID, "System", -1));
    }
    //TODO implement System methods entries
}
