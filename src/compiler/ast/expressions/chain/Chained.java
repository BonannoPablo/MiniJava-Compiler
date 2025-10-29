package compiler.ast.expressions.chain;

public abstract class Chained {
    protected Chained chain;

    public void addChain(Chained c){
        chain = c;
    }

    public abstract void print(int level);
}
