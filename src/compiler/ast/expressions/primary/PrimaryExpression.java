package compiler.ast.expressions.primary;

import compiler.ast.expressions.ExpressionNode;
import compiler.exceptions.SemanticException;
import compiler.symboltable.types.Type;

public class PrimaryExpression extends Primary{
    ExpressionNode expression;

    public PrimaryExpression(ExpressionNode expression) {
        this.expression = expression;
    }

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"PRIMARY EXPRESSION");
        expression.print(i+1);

        if(chained != null){
            chained.print(i+1);
        }
    }

    @Override
    public void check() throws SemanticException {
        expression.check();
    }

    @Override
    public Type getType() {
        return expression.getType();
    }
}
