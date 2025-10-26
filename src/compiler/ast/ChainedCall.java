package compiler.ast;

import compiler.token.Token;

public class ChainedCall extends Chained{

    Token call;

    public ChainedCall(Token call){
        this.call = call;
    }


    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"CHAINED ATTRIBUTE");
        System.out.println(" ".repeat(level)+"attribute name: " + call.getLexeme());
        if(chain != null)
            chain.print(level+1);
    }
}
