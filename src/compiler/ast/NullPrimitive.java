package compiler.ast;

import compiler.symboltable.types.Type;

public class NullPrimitive extends Primitive{

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"NULL PRIMITIVE");
    }

    @Override
    public Type getType() {
        return null;
    }
}
