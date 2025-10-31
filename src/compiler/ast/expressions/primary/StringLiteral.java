package compiler.ast.expressions.primary;

import compiler.symboltable.types.ClassType;
import compiler.symboltable.types.Type;
import compiler.token.Token;

public class StringLiteral extends Primary{

    Token value;

    public StringLiteral(Token value) {
        this.value = value;
    }

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"StringLiteral: " + value.getLexeme());

        if(chained != null){
            chained.print(i+1);
        }
    }

    @Override
    public void check() {}

    @Override
    public Type getType() {
        return new ClassType("String");
    }
}
