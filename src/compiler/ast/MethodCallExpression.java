package compiler.ast;

import compiler.token.Token;

import java.util.List;

public class MethodCallExpression {
    Token methodCalledToken;
    List<ExpressionNode> arguments;
    Chained chain;

    public MethodCallExpression(Token methodCalledToken){
        this.methodCalledToken = methodCalledToken;
    }

    public void addArguments(List<ExpressionNode> arguments){
        this.arguments = arguments;
    }

    public void addChain(Chained chain){
        this.chain = chain;
    }
