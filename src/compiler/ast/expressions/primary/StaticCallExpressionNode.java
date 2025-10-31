package compiler.ast.expressions.primary;

import compiler.ast.expressions.ExpressionNode;
import compiler.exceptions.SemanticException;
import compiler.symboltable.types.Type;
import compiler.token.Token;

import java.util.List;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class StaticCallExpressionNode extends Primary{
    Token classCalledToken;
    String classCalledName;
    Token methodCalledToken;
    List<ExpressionNode> arguments;


    public StaticCallExpressionNode(Token classCalledToken){
        this.classCalledToken = classCalledToken;
        classCalledName = classCalledToken.getLexeme();
    }

    public void addMethodCalled(Token methodCalledToken){
        this.methodCalledToken = methodCalledToken;
    }

    public void addArguments(List<ExpressionNode> arguments){
        this.arguments = arguments;
    }

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level)+"STATIC CALL EXPRESSION");
        System.out.println(" ".repeat(level)+"class called: " + classCalledName);
        System.out.println(" ".repeat(level)+"method called: " + methodCalledToken.getLexeme());
        for(ExpressionNode e : arguments){
            e.print(level+1);
        }
        if(chained != null){
            chained.print(level+1);
        }

    }

    @Override
    public void check() throws SemanticException {
        var classEntry = symbolTable.getClassEntry(classCalledName);
        if(classEntry != null){
            var method = classEntry.getMethod(arguments.size() +  methodCalledToken.getLexeme());
            if(method != null && method.getModifier() != null && method.getModifier().getTokenType() == Token.TokenType.STATIC_WORD){
                var parameters = method.getParameters();
                for(int i = 0; i < parameters.size(); i++){
                    if(!arguments.get(i).getType().getName().equals(parameters.get(i).getType().getName()))
                        throw new SemanticException("Static method does not accept given argument types", methodCalledToken);
                }
            } else
                throw new SemanticException("Static method does not exist", methodCalledToken);
        } else
            throw new SemanticException("Class does not exist", classCalledToken);
    }

    @Override
    public Type getType() {
        return symbolTable.getClassEntry(classCalledName).getMethod(arguments.size() +  methodCalledToken.getLexeme()).getReturnType();
    }
}
