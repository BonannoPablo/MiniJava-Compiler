package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.token.Token;

public class StringLiteral extends Primary{

    Token value;

    public StringLiteral(Token value) {
        this.value = value;
    }

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"StringLiteral: "+value);

        if(chain != null){
            chain.print(i+1);
        }
    }

    @Override
    public Type getType() {
        return null;
    }
}
