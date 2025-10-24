package compiler.ast;

public class WhileNode extends SentenceNode{
    ExpressionNode condition;
    SentenceNode body;

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
}
