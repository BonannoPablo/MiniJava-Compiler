package compiler.ast;

import compiler.symboltable.types.Type;

public abstract class ExpressionNode {
    public abstract void print(int i) ;


    public abstract Type getType();

}
