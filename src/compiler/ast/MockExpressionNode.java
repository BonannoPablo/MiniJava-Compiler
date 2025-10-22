package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.symboltable.types.UniversalType;
import compiler.token.Token;
import compiler.token.TokenImpl;

public class MockExpressionNode extends ExpressionNode{
    @Override
    public Type getType() {
        return new UniversalType();
    }
}
