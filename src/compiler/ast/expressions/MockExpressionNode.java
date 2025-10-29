package compiler.ast.expressions;

import compiler.symboltable.types.Type;
import compiler.symboltable.types.UniversalType;

public class MockExpressionNode extends ExpressionNode {
    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"MOCK EXPRESSION");
    }

    @Override
    public void check() {
        //TODO
    }

    @Override
    public Type getType() {
        return new UniversalType();
    }
}
