package compiler.ast;

public class IfNode extends SentenceNode {
    ExpressionNode condition;
    SentenceNode thenSentence;
    SentenceNode elseSentence;

    public IfNode() {
        condition = null;
        thenSentence = null;
        elseSentence = null;
    }

    public void addCondition(ExpressionNode e) {
        condition = e; //TODO check type is boolean
    }
    public void addThenSentence(SentenceNode s) {
        thenSentence = s;
    }
    public void addElseSentence(SentenceNode s) {
        elseSentence = s;
    }
    public void print(int level){
        System.out.println(" ".repeat(level)+"IF");
        if(condition != null) //TODO throw exception if there is no condition
            condition.print(level+1);
        if(thenSentence != null) //TODO throw exception if there is no then sentence
            thenSentence.print(level+1);
        if(elseSentence != null)
            elseSentence.print(level+1);
    }

}
