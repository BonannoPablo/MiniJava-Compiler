package compiler.ast.expressions.primary;

import compiler.symboltable.types.Type;
import compiler.token.Token;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class VarAccessNode extends Primary{
    Token varToken;
    String name;

    public VarAccessNode(Token varToken) {
        this.varToken = varToken;
        this.name = varToken.getLexeme();
    }


    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"VAR ACCESS NODE");
        System.out.println(" ".repeat(i)+"VarName: " + name);

        if(chained != null){
            chained.print(i+1);
        }
    }

    @Override
    public void check() {
        //TODO check chains
    }

    @Override
    public Type getType() {
        var variable = symbolTable.getCurrentBlock().getVariable(name);
        if(variable != null)
            return variable.getType();
        else
            return symbolTable.getCurrentMethod().getParameter(name).getType();
    }
}
