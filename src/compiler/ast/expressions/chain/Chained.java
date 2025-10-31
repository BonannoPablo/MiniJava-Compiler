package compiler.ast.expressions.chain;

import compiler.ast.expressions.ExpressionNode;
import compiler.token.Token;

public abstract class Chained {
    protected Chained chain;
    Token token;

    public void addChain(Chained c){
        chain = c;
    }

    public Chained getChain(){
        return chain;
    }

    public Token getToken(){
        return token;
    }

    public Chained getLastChained(){
        if(chain == null)
            return this;
        else
            return chain.getLastChained();
    }

    public abstract void print(int level);
}
