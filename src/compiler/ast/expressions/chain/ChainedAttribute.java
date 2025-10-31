package compiler.ast.expressions.chain;

import compiler.token.Token;

public class ChainedAttribute extends Chained{

    public ChainedAttribute(Token attributeName){
        this.token = attributeName;
    }


    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"CHAINED ATTRIBUTE");
        System.out.println(" ".repeat(level)+"attribute name: " + token.getLexeme());
        if(chain != null)
            chain.print(level+1);
    }
}
