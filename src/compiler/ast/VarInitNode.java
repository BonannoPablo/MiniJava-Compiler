package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.token.Token;

public class VarInitNode extends SentenceNode{
    Token token;
    String name;
    Type type;
    ExpressionNode expression;

    public void addToken(Token t) {
        token = t;
        name = token.getLexeme();
    }

    public void addExpression(ExpressionNode e){
        expression = e;
    }

        @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"VAR INIT");
        System.out.println(" ".repeat(level)+"name: " + name);
        expression.print(level+1);
    }
}
