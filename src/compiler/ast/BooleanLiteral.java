package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.token.Token;

public class BooleanLiteral extends Primitive{
    Token value;

    public BooleanLiteral(Token value) {
        this.value = value;
    }

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"BooleanLiteral: "+ value.getLexeme());
    }

    @Override
    public Type getType() {
        return null;
    }
}
