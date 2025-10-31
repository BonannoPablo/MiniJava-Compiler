package compiler.ast.expressions.primary;


import compiler.ast.expressions.chain.Chained;
import compiler.ast.expressions.ExpressionNode;

public abstract class Primary extends ExpressionNode {
    Chained chained;

    public void addChain(Chained c){
        chained = c;
    }

    public Chained getChained(){
        return chained;
    }

    public Chained getLastChained(){
        if(chained == null)
            return null;
        else
            return chained.getLastChained();
    }
}
