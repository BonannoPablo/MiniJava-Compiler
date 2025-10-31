package compiler.ast.expressions;

import compiler.exceptions.SemanticException;
import compiler.symboltable.types.PrimitiveType;
import compiler.symboltable.types.Type;
import compiler.token.Token;

public class UnaryExpressionNode extends ExpressionNode {
    Token unaryOperator;
    ExpressionNode expression;

    public UnaryExpressionNode(Token unaryOperator, ExpressionNode expression) {
        this.unaryOperator = unaryOperator;
        this.expression = expression;
    }

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"UNARY EXPRESSION");
        System.out.println(" ".repeat(i)+"UnaryOperator: "+unaryOperator.getLexeme());
        expression.print(i+1);
    }

    @Override
    public void check() throws SemanticException {
        expression.check();
        var expressionType = expression.getType().getName();
        switch (unaryOperator.getTokenType()){
            case PLUS:
            case MINUS:
            case PLUS1:
            case MINUS1:{
                if(! expressionType.equals("int"))
                    throw new SemanticException("Invalid type for operator " + unaryOperator.getLexeme(), unaryOperator);
                break;
            }
            case EXCLAMATION_POINT:
                if(! expressionType.equals("boolean"))
                    throw new SemanticException("Invalid type for operator " + unaryOperator.getLexeme(), unaryOperator);
        }
    }

    @Override
    public Type getType() {
        if(unaryOperator.getTokenType() == Token.TokenType.EXCLAMATION_POINT)
            return new PrimitiveType( "boolean");
        else
            return new PrimitiveType("int");
    }
}
