package compiler.ast.expressions.primitive;

import compiler.symboltable.types.PrimitiveType;
import compiler.symboltable.types.Type;
import compiler.token.Token;

public class CharLiteral extends Primitive{

    Token value;

    public CharLiteral(Token value) {
        this.value = value;
    }

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"CharLiteral: " + value.getLexeme());
    }

    @Override
    public void check() {
        //TODO
    }

    @Override
    public Type getType() {
        return new PrimitiveType("char");
    }
}
