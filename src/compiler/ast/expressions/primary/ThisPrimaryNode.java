package compiler.ast.expressions.primary;

import compiler.exceptions.SemanticException;
import compiler.symboltable.types.ClassType;
import compiler.symboltable.types.Type;
import compiler.token.Token;
import compiler.token.TokenImpl;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class ThisPrimaryNode extends Primary{


    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"THIS PRIMARY NODE");

        if(chained != null){
            chained.print(i+1);
        }
    }

    @Override
    public void check() throws SemanticException {
        var modifier = symbolTable.getCurrentMethod().getModifier();
        if(modifier != null && modifier.getTokenType() == Token.TokenType.STATIC_WORD)
            throw new SemanticException("This cannot be invoked in static methods", new TokenImpl(Token.TokenType.THIS_WORD, "placeholder", -1)); //TODO change token
    }

    @Override
    public Type getType() {
        return new ClassType(symbolTable.getCurrentClass().getName());
    }
}
