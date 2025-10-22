package compiler.ast;

import compiler.token.Token;

import java.util.List;

public class StaticCallNode extends SentenceNode{
    Token classCalledToken;
    String classCalledName;
    Token methodCalledToken;
    List<ExpressionNode> arguments;


    public StaticCallNode(Token classCalledToken){
        this.classCalledToken = classCalledToken;
        classCalledName = classCalledToken.getLexeme();
    }

    public void addMethodCalled(Token methodCalledToken){
        this.methodCalledToken = methodCalledToken;
    }

    public void addArguments(List<ExpressionNode> arguments){
        this.arguments = arguments;
    }
    @Override
    public void print(int level) {

    }


}
