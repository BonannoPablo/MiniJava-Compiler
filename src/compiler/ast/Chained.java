package compiler.ast;

public abstract class Chained {
    protected Chained chain;

    public void addChain(Chained c){
        chain = c;
    }

    public abstract void print(int level);
}
