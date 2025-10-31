package compiler.ast.expressions.primary;

import compiler.exceptions.SemanticException;
import compiler.symboltable.types.Type;
import compiler.token.Token;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class AttributeAccessNode extends Primary{
    Token token;
    String name;

    public AttributeAccessNode(Token token) {
        this.token = token;
        this.name = token.getLexeme();
    }

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"ATTRIBUTE ACCESS NODE");
        System.out.println(" ".repeat(i)+"attribute: " + name);

        if(chained != null){
            chained.print(i+1);
        }
    }

    @Override
    public void check() throws SemanticException {
        //TODO
    }

    @Override
    public Type getType() {
        return symbolTable.getCurrentClass().getAttribute(name).getType();
    }
}
