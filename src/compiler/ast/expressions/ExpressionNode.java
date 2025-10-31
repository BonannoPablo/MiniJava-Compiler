package compiler.ast.expressions;

import compiler.exceptions.SemanticException;
import compiler.symboltable.types.Type;

public abstract class ExpressionNode {

    public abstract void print(int i) ;

    public abstract void check() throws SemanticException;

    public abstract Type getType();

}
