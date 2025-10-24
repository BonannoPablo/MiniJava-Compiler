package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.token.Token;

public class UnaryExpressionNode extends ExpressionNode{
    Token unaryOperator;
    ExpressionNode expression;

    public UnaryExpressionNode(Token unaryOperator, ExpressionNode expression) {
        this.unaryOperator = unaryOperator;
        this.expression = expression;
    }

    @Override
    public void print(int i) {

    }

    @Override
    public Type getType() {
        return null;
    }
}
