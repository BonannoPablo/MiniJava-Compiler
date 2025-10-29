package compiler.ast.expressions.primary;

import compiler.ast.expressions.ExpressionNode;
import compiler.symboltable.types.Type;
import compiler.token.Token;

import java.util.List;

public class ConstructorCallNode extends Primary{
    Token className;
    //TODO add generic type or diamond
    List<ExpressionNode> arguments;

    public ConstructorCallNode(Token className) {
        this.className = className;
    }

    public void addArguments(List<ExpressionNode> arguments) {
        this.arguments = arguments;
    }

    @Override
    public void print(int i) {
        System.out.println(" ".repeat(i)+"CONSTRUCTOR CALL: ");
        System.out.println(" ".repeat(i)+"ClassName: " + className.getLexeme());
        System.out.println(" ".repeat(i)+"Arguments: ");
        for(ExpressionNode arg: arguments){
            arg.print(i+1);
        }

        if(chain != null){
            chain.print(i+1);
        }
    }

    @Override
    public void check() {
        //TODO
    }

    @Override
    public Type getType() {
        return null;
    }
}
