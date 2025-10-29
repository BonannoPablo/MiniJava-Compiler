package compiler.ast.expressions.primitive;

import compiler.symboltable.types.PrimitiveType;
import compiler.symboltable.types.Type;

public class NullPrimitive extends Primitive{

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"NULL PRIMITIVE");
    }

    @Override
    public void check() {}

    @Override
    public Type getType() {
        return new PrimitiveType("null");
    }
}
