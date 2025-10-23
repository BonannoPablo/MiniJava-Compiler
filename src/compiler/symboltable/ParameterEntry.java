package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.symboltable.types.Type;
import compiler.token.Token;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class ParameterEntry {
    Token token;
    String name;
    Type type;

    public ParameterEntry(Type type, Token token) {
        this.type = type;
        this.token = token;
        name = token.getLexeme();
    }

    public void checkDeclaration() throws SemanticException {
      /*  Token containerClassGenericType = symbolTable.getCurrentClass().getGenericType();
        boolean genericTypeMatch = containerClassGenericType != null && type.getToken().getLexeme().equals(containerClassGenericType.getLexeme());
        boolean existsTypeClass = symbolTable.existsClass(type.getName());

        if(type.getToken().getTokenType() == Token.TokenType.CLASSID
        && ! (existsTypeClass || genericTypeMatch))
                throw new SemanticException("Class does not exist", type.getToken()); //TODO change msg

        boolean hasGenericType = !type.getGenericType().isEmpty();
        boolean existsGenericType = symbolTable.existsClass(type.getGenericType());

        if( hasGenericType &&
           ! (existsGenericType || (containerClassGenericType != null && type.getGenericType().equals(containerClassGenericType.getLexeme()))))
            throw new SemanticException("Class does not exist", type.getGenericTypeToken());

        boolean declaredClassHasGenericType = existsTypeClass && symbolTable.getClassEntry(type.getName()).getGenericType() != null;
        boolean parameterTypeHasGenericType = !type.getGenericType().isEmpty() && symbolTable.getClassEntry(type.getGenericType()).getGenericType() != null;

        if(true){}
        //TODO finish parameter checking
*/




        if(type.getToken().getTokenType() == Token.TokenType.CLASSID) {

            Token containerClassGenericType = symbolTable.getCurrentClass().getGenericType();
            boolean typeIsClassGenericType = containerClassGenericType != null && type.getToken().getLexeme().equals(containerClassGenericType.getLexeme());
            boolean existsTypeClass = symbolTable.existsClass(type.getName());

            //Check if parameter type is declared or is the class generic type
            if (!(existsTypeClass || typeIsClassGenericType))
                throw new SemanticException("Class does not exist", type.getToken());

            if(!typeIsClassGenericType) {
                boolean typeClassHasGenericType = symbolTable.getClassEntry(type.getName()).getGenericType() != null;
                boolean parameterTypeHasGenericType = !type.getGenericType().isEmpty();

                //Check if declared type in parameter correctly uses generics
                if (typeClassHasGenericType && !parameterTypeHasGenericType)
                    throw new SemanticException("Class requires generic type", type.getToken());
                if (!typeClassHasGenericType && parameterTypeHasGenericType)
                    throw new SemanticException("Class does not accept generic types", type.getToken());

                //Check if the generic type exists
                if (parameterTypeHasGenericType) {
                    boolean existsGenericTypeClass = symbolTable.existsClass(type.getGenericType());
                    boolean containerClassGenericTypeMatch = containerClassGenericType != null && type.getGenericType().equals(containerClassGenericType.getLexeme());
                    if (!(existsGenericTypeClass || containerClassGenericTypeMatch))
                        throw new SemanticException("Class does not exist", type.getToken());
                }

            }
        }


    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Token getToken() {
        return token;
    }
}
