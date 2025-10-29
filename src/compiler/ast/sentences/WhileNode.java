package compiler.ast.sentences;

import compiler.ast.expressions.ExpressionNode;
import compiler.exceptions.SemanticException;
import compiler.token.Token;

public class WhileNode extends SentenceNode {
    Token token;
    ExpressionNode condition;
    SentenceNode body;

    public WhileNode(Token token){
        this.token = token;
    }

    @Override
    public void print(int level){
        System.out.println(" ".repeat(level)+"WHILE");
        if(condition != null) {
            System.out.println(" ".repeat(level)+"condition");
            condition.print(level + 1);
        }
        if(body != null) {
            System.out.println(" ".repeat(level)+"body");
            body.print(level + 1);
        }
    }

    public void addCondition(ExpressionNode s){
        condition = s;
    }

    public void addSentence(SentenceNode s){
        body = s;
    }

    public void check() throws SemanticException {
        condition.check();
        if (! condition.getType().getName().equals("boolean"))
            throw new SemanticException("Condition must be boolean", token);
        body.check();
    }
}
