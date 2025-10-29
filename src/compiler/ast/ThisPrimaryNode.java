package compiler.ast;

import compiler.symboltable.types.Type;

public class ThisPrimaryNode extends Primary{


    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"THIS PRIMARY NODE");

        if(chain != null){
            chain.print(i+1);
        }
    }

    @Override
    public Type getType() {
        return null;
    }
}
