package compiler.symboltable.types;

import compiler.token.Token;
import compiler.token.TokenImpl;

public class UniversalType extends Type {
    public UniversalType() {
        super("");
    }

    @Override
    public Token getGenericTypeToken() {
        return null;
    }

    @Override
    public boolean conforms(Type t){
        return true;
    }
}
