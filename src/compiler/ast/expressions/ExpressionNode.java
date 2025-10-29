package compiler.ast.expressions;

import compiler.symboltable.types.Type;

public abstract class ExpressionNode {

    public abstract void print(int i) ;

    public abstract void check();

    public abstract Type getType();

}
