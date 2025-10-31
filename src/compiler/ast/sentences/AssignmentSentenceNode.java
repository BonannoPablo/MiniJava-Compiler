package compiler.ast.sentences;

import compiler.ast.expressions.AssignmentExpressionNode;
import compiler.ast.expressions.ExpressionNode;
import compiler.exceptions.SemanticException;
import compiler.token.Token;
import compiler.token.TokenImpl;

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
        if(assignment instanceof AssignmentExpressionNode)
            assignment.check();
        else
            throw new SemanticException("Not a sentence", new TokenImpl(Token.TokenType.PUBLIC_WORD,"placeholder",-1)); //TODO setup exception token
    }
}
