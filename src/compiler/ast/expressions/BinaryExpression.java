package compiler.ast.expressions;

import compiler.symboltable.types.Type;
import compiler.token.Token;

public class BinaryExpression extends ExpressionNode {
    ExpressionNode left;
    ExpressionNode right;
    Token operator;

    public BinaryExpression(ExpressionNode left, ExpressionNode right, Token operator){
        this.left = left;
        this.right = right;
        this.operator = operator;
    }


    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"BINARY EXPRESSION");
        System.out.println(" ".repeat(level)+"left expression:");
        left.print(level+1);
        System.out.println(" ".repeat(level)+"right expression:");
        right.print(level+1);
        System.out.println(" ".repeat(level)+"operator:");
        System.out.println(" ".repeat(level+1)+operator.getLexeme());
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
