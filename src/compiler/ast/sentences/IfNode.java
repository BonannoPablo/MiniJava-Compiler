package compiler.ast.sentences;

import compiler.ast.expressions.ExpressionNode;
import compiler.exceptions.SemanticException;
import compiler.token.Token;

public class IfNode extends SentenceNode {
    Token token;
    ExpressionNode condition;
    SentenceNode thenSentence;
    SentenceNode elseSentence;

    public IfNode(Token token) {
        this.token = token;
    }

    public void addCondition(ExpressionNode e) {
        condition = e;
    }
    public void addThenSentence(SentenceNode s) {
        thenSentence = s;
    }
    public void addElseSentence(SentenceNode s) {
        elseSentence = s;
    }
    public void print(int level){
        System.out.println(" ".repeat(level)+"IF");
        if(condition != null) {
            System.out.println(" ".repeat(level)+"condition");
            condition.print(level + 1);
        }
        if(thenSentence != null) {
            System.out.println(" ".repeat(level)+"then sentence");
            thenSentence.print(level + 1);
        }
        if(elseSentence != null) {
            System.out.println(" ".repeat(level)+"else sentence");
            elseSentence.print(level + 1);
        }
    }

    @Override
    public void check() throws SemanticException {
        condition.check();
        if (! condition.getType().getName().equals("boolean"))
            throw new SemanticException("Condition must be boolean", token);
        thenSentence.check();
        if(elseSentence != null)
            elseSentence.check();
    }

}
