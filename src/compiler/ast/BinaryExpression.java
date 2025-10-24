package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.token.Token;

public class BinaryExpression extends ExpressionNode{
    ExpressionNode left;
    ExpressionNode right;
    Token operator;

    public BinaryExpression(ExpressionNode left, ExpressionNode right, Token operator){
        this.left = left;
        this.right = right;
        this.operator = operator;
    }


    @Override
    public void print(int i) {

    }

    @Override
    public Type getType() {
        return null;
    }
}
