package compiler.ast.expressions.primary;

import compiler.ast.expressions.ExpressionNode;
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

        if(chain != null){
            chain.print(i+1);
        }
    }

    @Override
    public void check() {
        //TODO
    }

    @Override
    public Type getType() {
        return null;
    }
}
