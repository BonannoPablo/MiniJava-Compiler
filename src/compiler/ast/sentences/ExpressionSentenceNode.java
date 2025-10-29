package compiler.ast.sentences;

import compiler.ast.expressions.ExpressionNode;
import compiler.exceptions.SemanticException;

public class ExpressionSentenceNode extends SentenceNode {
    ExpressionNode expression;

    public ExpressionSentenceNode(ExpressionNode expression){
        this.expression = expression;
    }

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"EXPRESSION SENTENCE");
        expression.print(level+1);
    }

    @Override
    public void check() throws SemanticException {
        //TODO
    }
}
