package compiler.ast.expressions.primary;


import compiler.ast.expressions.chain.Chained;
import compiler.ast.expressions.ExpressionNode;

public abstract class Primary extends ExpressionNode {
    Chained chain;

    public void addChain(Chained c){
        chain = c;
    }
}
