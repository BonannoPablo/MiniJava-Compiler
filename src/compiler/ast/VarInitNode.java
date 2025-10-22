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
        //TODO
    }
}
