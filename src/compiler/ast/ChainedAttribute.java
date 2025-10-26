package compiler.ast;

import compiler.token.Token;

public class ChainedAttribute extends Chained{

    Token attributeName;

    public ChainedAttribute(Token attributeName){
        this.attributeName = attributeName;
    }


    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"CHAINED ATTRIBUTE");
        System.out.println(" ".repeat(level)+"attribute name: " + attributeName.getLexeme());
        if(chain != null)
            chain.print(level+1);
    }
}
