package compiler.ast.sentences;

import compiler.ast.expressions.ExpressionNode;
import compiler.exceptions.SemanticException;

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

    @Override
    public void check() throws SemanticException {
        //TODO
    }


}
