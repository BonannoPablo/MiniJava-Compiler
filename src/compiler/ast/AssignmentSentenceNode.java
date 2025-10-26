package compiler.ast;

public class AssignmentSentenceNode extends SentenceNode {
    ExpressionNode assignment;

    public AssignmentSentenceNode(ExpressionNode assignment){
        this.assignment = assignment;
    }

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"ASSIGNMENT SENTENCE");
        assignment.print(level+1);
    }




}
