package compiler.ast.expressions.chain;

import compiler.ast.expressions.ExpressionNode;
import compiler.ast.expressions.primary.MethodCallExpression;
import compiler.token.Token;

public class ChainedCall extends Chained{
    MethodCallExpression methodCall;

    public ChainedCall(Token call, MethodCallExpression expression){
        this.token = call;
        this.methodCall = expression;
    }

    public MethodCallExpression getMethodCall(){
        return methodCall;
    }

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"CHAINED CALL");
        System.out.println(" ".repeat(level)+"method name: " + token.getLexeme());
        if(chain != null)
            chain.print(level+1);
    }
}
