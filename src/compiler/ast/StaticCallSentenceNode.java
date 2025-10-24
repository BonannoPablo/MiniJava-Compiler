package compiler.ast;

import compiler.token.Token;

import java.util.List;

public class StaticCallSentenceNode extends SentenceNode{
    ExpressionNode expression;

    public StaticCallSentenceNode(ExpressionNode expression){
        this.expression = expression;
    }

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"STATIC CALL SENTENCE");
        expression.print(level+1);
    }
}
