package compiler.ast.sentences;

import compiler.ast.expressions.AssignmentExpressionNode;
import compiler.ast.expressions.ExpressionNode;
import compiler.ast.expressions.chain.Chained;
import compiler.ast.expressions.chain.ChainedCall;
import compiler.ast.expressions.primary.MethodCallExpression;
import compiler.ast.expressions.primary.Primary;
import compiler.exceptions.SemanticException;
import compiler.token.Token;
import compiler.token.TokenImpl;

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
        expression.check();

        Chained c = expression instanceof Primary ? ((Primary) expression).getChained() : null;

        while (c != null && c.getChain() != null) {
            c = c.getChain();
        }
        if(c != null && ! (c instanceof ChainedCall))
            throw new SemanticException("Not a statement", c.getToken());
        else if(c == null && ! (expression instanceof MethodCallExpression)){
            throw new SemanticException("Not a statement", new TokenImpl(Token.TokenType.PUBLIC_WORD,"placeholder",-1));//TODO define token to throw exception
        }


    }
}
