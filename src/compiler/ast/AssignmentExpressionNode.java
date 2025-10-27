package compiler.ast;

import compiler.symboltable.types.Type;

public class AssignmentExpressionNode extends ExpressionNode{
    ExpressionNode leftSide;
    ExpressionNode rightSide;

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"ASSIGNMENT EXPRESSION");
        System.out.println(" ".repeat(level)+"left side:");
        leftSide.print(level+1);
        System.out.println(" ".repeat(level)+"right side:");
        rightSide.print(level+1);
    }

    @Override
    public Type getType() {
        return null;
    }

    public void addLeftSide(ExpressionNode e) {
        leftSide = e;
    }
    public void addRightSide(ExpressionNode e) {
        rightSide = e;
    }

}
