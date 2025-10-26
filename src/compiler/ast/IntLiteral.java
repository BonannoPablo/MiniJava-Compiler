package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.token.Token;

public class IntLiteral extends Primitive{
    Token value;

    public IntLiteral(Token value) {
        this.value = value;
    }

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"IntLiteral: " + value.getLexeme());
    }

    @Override
    public Type getType() {
        return null;
    }
}
