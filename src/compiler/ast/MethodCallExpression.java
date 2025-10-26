package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.token.Token;

import java.util.List;

public class MethodCallExpression extends Primary {
    Token methodCalledToken;
    List<ExpressionNode> arguments;
    Chained chain = null;

    public MethodCallExpression(Token methodCalledToken) {
        this.methodCalledToken = methodCalledToken;
    }

    public void addArguments(List<ExpressionNode> arguments) {
        this.arguments = arguments;
    }

    public void addChain(Chained chain) {
        this.chain = chain;
    }

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"METHOD CALL EXPRESSION");
        System.out.println(" ".repeat(i)+"method called: " + methodCalledToken.getLexeme());
        for(ExpressionNode e : arguments){
            e.print(i+1);
        }
        if(chain != null){
            chain.print(i+1);
        }
    }

    @Override
    public Type getType() {
        return null;
    }
}
