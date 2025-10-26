package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.token.Token;

public class VarAccessNode extends Primary{
    Token varName;


    public VarAccessNode(Token varName) {
        this.varName = varName;
    }


    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"VAR ACCESS NODE");
        System.out.println(" ".repeat(i)+"VarName: "+varName);
    }

    @Override
    public Type getType() {
        return null;
    }
}
