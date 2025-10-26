package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.token.Token;

public class CharLiteral extends Primitive{

    Token value;

    public CharLiteral(Token value) {
        this.value = value;
    }

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"CharLiteral: "+value);
    }

    @Override
    public Type getType() {
        return null;
    }
}
