package compiler.ast;

import compiler.symboltable.types.Type;

public class ThisPrimaryNode extends Primary{


    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"THIS PRIMARY NODE");
    }

    @Override
    public Type getType() {
        return null;
    }
}
