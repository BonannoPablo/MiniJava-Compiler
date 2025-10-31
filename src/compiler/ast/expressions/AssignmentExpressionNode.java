package compiler.ast.expressions;

import compiler.ast.expressions.chain.ChainedAttribute;
import compiler.ast.expressions.primary.AttributeAccessNode;
import compiler.ast.expressions.primary.Primary;
import compiler.ast.expressions.primary.VarAccessNode;
import compiler.exceptions.SemanticException;
import compiler.symboltable.types.Type;
import compiler.token.Token;
import compiler.token.TokenImpl;

public class AssignmentExpressionNode extends ExpressionNode {
    ExpressionNode leftSide;
    ExpressionNode rightSide;

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"ASSIGNMENT EXPRESSION");
        System.out.println(" ".repeat(level)+"left side:");
        leftSide.print(level+1);
        System.out.println(" ".repeat(level)+"right side:");
        rightSide.print(level+1);
    }

    @Override
    public void check() throws SemanticException {
        leftSide.check();
        rightSide.check();


        if(leftSide instanceof Primary && ((Primary) leftSide).getChained() != null && ! (((Primary) leftSide).getLastChained() instanceof ChainedAttribute))
            throw new SemanticException("Invalid left side", ((Primary) leftSide).getChained().getToken());
        if((leftSide instanceof AttributeAccessNode || leftSide instanceof VarAccessNode) && ! rightSide.getType().conforms(leftSide.getType())){
            throw new SemanticException("Right side has invalid type", new TokenImpl(Token.TokenType.PUBLIC_WORD,"placeholder",-1));
        }

    }

    @Override
    public Type getType() {
        return null;
    }

    public void addLeftSide(ExpressionNode e) {
        leftSide = e;
    }
    public void addRightSide(ExpressionNode e) {
        rightSide = e;
    }

}
