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
        if(condition != null) { //TODO throw exception if there is no condition
            System.out.println(" ".repeat(level)+"condition");
            condition.print(level + 1);
        }
        if(thenSentence != null) {//TODO throw exception if there is no then sentence
            System.out.println(" ".repeat(level)+"then sentence");
            thenSentence.print(level + 1);
        }
        if(elseSentence != null) {
            System.out.println(" ".repeat(level)+"else sentence");
            elseSentence.print(level + 1);
        }
    }

}
