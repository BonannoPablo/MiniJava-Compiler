package compiler.ast;


public abstract class Primary extends ExpressionNode{
    Chained chain;

    public void addChain(Chained c){
        chain = c;
    }
}
