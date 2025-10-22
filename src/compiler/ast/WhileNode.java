package compiler.ast;

public class WhileNode extends SentenceNode{
    ExpressionNode condition;
    SentenceNode body;

    @Override
    public void print(int level){
        System.out.println(" ".repeat(level)+"WHILE");
        if(condition != null)
            condition.print(level+1);
        if(body != null)
            body.print(level+1);
    }

    public void addCondition(ExpressionNode s){
        condition = s;
    }

    public void addSentence(SentenceNode s){
        body = s;
    }
}
