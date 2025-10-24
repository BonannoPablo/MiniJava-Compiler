package compiler.ast;

public class ExpressionSentenceNode extends SentenceNode{
    ExpressionNode expression;

    public ExpressionSentenceNode(ExpressionNode expression){
        this.expression = expression;
    }

    @Override
    public void print(int level) {

    }
}
