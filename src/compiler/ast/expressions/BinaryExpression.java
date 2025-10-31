package compiler.ast.expressions;

import compiler.exceptions.SemanticException;
import compiler.symboltable.types.PrimitiveType;
import compiler.symboltable.types.Type;
import compiler.token.Token;

public class BinaryExpression extends ExpressionNode {
    ExpressionNode left;
    ExpressionNode right;
    Token operator;

    public BinaryExpression(ExpressionNode left, ExpressionNode right, Token operator){
        this.left = left;
        this.right = right;
        this.operator = operator;
    }


    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"BINARY EXPRESSION");
        System.out.println(" ".repeat(level)+"left expression:");
        left.print(level+1);
        System.out.println(" ".repeat(level)+"right expression:");
        right.print(level+1);
        System.out.println(" ".repeat(level)+"operator:");
        System.out.println(" ".repeat(level+1)+operator.getLexeme());
    }

    @Override
    public void check() throws SemanticException {
        left.check();
        right.check();

        switch (operator.getTokenType()){
            case PLUS:
            case MINUS:
            case MULTIPLY:
            case SLASH:
            case PERCENT:
            case GREATER_THAN:
            case LESS_THAN:
            case EQUAL_GREATER_THAN:
            case EQUAL_LESS_THAN:
                if(! left.getType().getName().equals("int") || ! right.getType().getName().equals("int"))
                    throw new SemanticException("Invalid type for operator " + operator.getLexeme(), operator);
                break;
            case OR:
            case AND:
                if(! left.getType().getName().equals("boolean") || ! right.getType().getName().equals("boolean"))
                    throw new SemanticException("Invalid type for operator " + operator.getLexeme(), operator);
                break;
            case EQUALS_COMPARISON:
            case DIFERENT:
                if(! left.getType().conforms(right.getType()) || ! right.getType().conforms(left.getType()))
                    throw new SemanticException("Invalid type for operator " + operator.getLexeme(), operator);
        }
    }

    @Override
    public Type getType() {
        return switch (operator.getTokenType()) {
            case PLUS, MINUS, MULTIPLY, SLASH, PERCENT -> new PrimitiveType("int");
            case GREATER_THAN, LESS_THAN, EQUAL_GREATER_THAN, EQUAL_LESS_THAN, OR, AND, EQUALS_COMPARISON, DIFERENT ->
                    new PrimitiveType("boolean");
            default -> null;
        };
    }
}
