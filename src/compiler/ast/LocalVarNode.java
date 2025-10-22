package compiler.ast;

import compiler.symboltable.types.Type;
import compiler.token.Token;

public class LocalVarNode {
    Token token;
    String name;
    Type type;

    public void addToken(Token t){
        token = t;
    }
}
