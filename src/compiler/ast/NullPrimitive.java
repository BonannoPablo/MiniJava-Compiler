package compiler.ast;

import compiler.symboltable.types.Type;

public class NullPrimitive extends Primitive{

    @Override
    public void print(int i) {

    }

    @Override
    public Type getType() {
        return null;
    }
}
