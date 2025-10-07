package compiler.symboltable;

import compiler.token.Token;
import compiler.token.TokenImpl;

public class ObjectEntry extends  ClassEntry{
    public ObjectEntry() {
        super(new TokenImpl(Token.TokenType.CLASSID, "Object", -1));
    }
    //TODO implement Object methods entries

}
