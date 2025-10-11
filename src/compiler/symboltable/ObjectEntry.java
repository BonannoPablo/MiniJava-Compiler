package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;
import compiler.token.TokenImpl;

import java.util.LinkedList;
import java.util.List;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class ObjectEntry extends  ClassEntry{
    public ObjectEntry() {
        super(new TokenImpl(Token.TokenType.CLASSID, "Object", -1));
        generateMethods();
    }

    private void generateMethods(){
        try {
            Token staticToken = new TokenImpl(Token.TokenType.STATIC_WORD, "static", -1);
            Type voidType = new PrimitiveType(new TokenImpl(Token.TokenType.VOID_WORD, "void", -1));
            addMethod( new MethodEntry(new TokenImpl(Token.TokenType.METVARID, "debugPrint", -1)));
            getCurrentMethod().setReturnType(voidType);
            getCurrentMethod().setModifier(staticToken);
            Type intType = new PrimitiveType(new TokenImpl(Token.TokenType.INT_WORD, "int", -1));
            getCurrentMethod().addParameter(new ParameterEntry(intType, new TokenImpl(Token.TokenType.METVARID, "i", -1)));

        }catch(SemanticException e) {
            //This catch block shouldn't be reachable
        }
    }

    @Override
    public void consolidate() { //No need to do anything to consolidate Object Class
    }
}
