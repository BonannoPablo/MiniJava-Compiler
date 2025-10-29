package compiler.ast.sentences;

import compiler.ast.expressions.ExpressionNode;
import compiler.exceptions.SemanticException;
import compiler.token.Token;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class ReturnNode extends SentenceNode {
    ExpressionNode expression;
    Token token;

    public ReturnNode(Token token) {
        this.token = token;
    }

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"RETURN");
        if(expression != null)
            expression.print(level+1);
    }

    @Override
    public void check() throws SemanticException {
        var currentMethodReturnType = symbolTable.getCurrentClass().getCurrentMethod().getReturnType().getName();
        if((expression == null && ! currentMethodReturnType.equals("void")) ||
            (expression != null && ! currentMethodReturnType.equals(expression.getType().getName()))) //TODO I should consider inheritance types here
            throw new SemanticException("Return type must match method signature", token);

    }

    public void addReturnExpression(ExpressionNode e) {
        expression = e;
    }
}
