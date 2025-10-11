package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;
import compiler.token.TokenImpl;

public class StringEntry extends ClassEntry{
    public StringEntry() {
        super(new TokenImpl(Token.TokenType.CLASSID, "String", -1));
    }

    @Override
    public void consolidate() {
        //No need to do anything to consolidate Object Class
    }

    public void checkDeclaration() throws SemanticException {
        //No need to check declaration
    }
}
