package compiler.symboltable.types;

import compiler.token.Token;
import compiler.token.TokenImpl;

public class UniversalType extends Type {
    public UniversalType() {
        super(null);
        name = "";
    }

    @Override
    public Token getGenericTypeToken() {
        return null;
    }
}
