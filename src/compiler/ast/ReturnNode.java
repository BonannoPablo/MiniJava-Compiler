package compiler.ast;

public class ReturnNode extends SentenceNode{
    ExpressionNode expression;

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"RETURN");
        if(expression != null)
            expression.print(level+1);
    }

    public void addReturnExpression(ExpressionNode e) {
        expression = e;
    }
}
