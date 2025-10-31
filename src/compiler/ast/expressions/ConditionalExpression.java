package compiler.ast.expressions;

import compiler.exceptions.SemanticException;
import compiler.symboltable.types.Type;
import compiler.token.Token;
import compiler.token.TokenImpl;

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
    public void check() throws SemanticException {
        condition.check();
        if(!condition.getType().getName().equals("boolean"))
            throw new SemanticException("Condition must be boolean", new TokenImpl(Token.TokenType.PUBLIC_WORD, "placeholder", -1)); //TODO change token

        trueExpression.check();
        falseExpression.check();

        if(trueExpression.getType().getName().equals("void") || falseExpression.getType().getName().equals("void"))
            throw new SemanticException("Expression expected", new TokenImpl(Token.TokenType.PUBLIC_WORD, "placeholder", -1));
        if(! trueExpression.getType().getName().equals(falseExpression.getType().getName()))
            throw new SemanticException("Result of conditional expressions must have the same type", new TokenImpl(Token.TokenType.PUBLIC_WORD, "placeholder", -1)); //TODO change token


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
