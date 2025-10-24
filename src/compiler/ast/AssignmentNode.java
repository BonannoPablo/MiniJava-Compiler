package compiler.ast;

public class AssignmentNode extends SentenceNode {
    ExpressionNode leftSide;
    ExpressionNode rightSide;

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"ASSIGNMENT");
        System.out.println(" ".repeat(level)+"left side:");
        leftSide.print(level+1);
        System.out.println(" ".repeat(level)+"right side:");
        rightSide.print(level+1);
    }

    public void addLeftSide(ExpressionNode e) {
        leftSide = e;
    }
    public void addRightSide(ExpressionNode e) {
        rightSide = e;
    }



}
