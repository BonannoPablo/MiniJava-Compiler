package compiler.ast.sentences;

import compiler.ast.expressions.ExpressionNode;
import compiler.exceptions.SemanticException;
import compiler.symboltable.types.Type;
import compiler.token.Token;

public class VarInitNode extends SentenceNode {
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
        System.out.println(" ".repeat(level)+"type: " + type.getName());
        expression.print(level+1);
    }

    @Override
    public void check() throws SemanticException {
        expression.check();
        type = expression.getType();
        if(type.getName().equals("null"))
            throw new SemanticException("Expression must not be null", token);
    }

    public Type getType(){
        return type;
    }

    public String getName() {
        return name;
    }

    public Token getToken() {
        return token;
    }
}
