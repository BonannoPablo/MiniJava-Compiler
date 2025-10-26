package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.token.Token;

import java.util.List;

public class StaticCallExpressionNode extends Primary{
    Token classCalledToken;
    String classCalledName;
    Token methodCalledToken;
    List<ExpressionNode> arguments;
    Chained chain;


    public StaticCallExpressionNode(Token classCalledToken){
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
        System.out.println(" ".repeat(level)+"STATIC CALL EXPRESSION");
        System.out.println(" ".repeat(level)+"class called: " + classCalledName);
        System.out.println(" ".repeat(level)+"method called: " + methodCalledToken.getLexeme());
        for(ExpressionNode e : arguments){
            e.print(level+1);
        }
        if(chain != null){
            chain.print(level+1);
        }

    }

    @Override
    public Type getType() {
        return null;
    }
}
