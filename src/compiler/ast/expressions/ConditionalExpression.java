package compiler.ast.expressions;

import compiler.symboltable.types.Type;

public class ConditionalExpression extends ExpressionNode {
    ExpressionNode condition;
    ExpressionNode trueExpression;
    ExpressionNode falseExpression;


    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"CONDITIONAL EXPRESSION");
        System.out.println(" ".repeat(i)+"condition");
        condition.print(i+1);
        System.out.println(" ".repeat(i)+"true expression");
        trueExpression.print(i+1);
        System.out.println(" ".repeat(i)+"false expression");
        falseExpression.print(i+1);
    }

    @Override
    public void check() {
        //TODO
    }

    @Override
    public Type getType() {
        return null;
    }

    public void addCondition(ExpressionNode e){
        condition = e;
    }
    public void addTrueExpression(ExpressionNode e){
        trueExpression = e;
    }
    public void addFalseExpression(ExpressionNode e){
        falseExpression = e;
    }

}
