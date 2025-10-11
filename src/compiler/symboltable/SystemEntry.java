package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;
import compiler.token.TokenImpl;

import java.util.LinkedList;
import java.util.List;

public class SystemEntry extends ClassEntry{

    public SystemEntry() {
        super(new TokenImpl(Token.TokenType.CLASSID, "System", -1));
        generateMethods();
    }
    //TODO implement System methods entries

    private void generateMethods() {
        Type voidType = new PrimitiveType(new TokenImpl(Token.TokenType.VOID_WORD, "void", -1));
        try {
            createMethodRead();
            createMethodPrintB(voidType);
            createMethodPrintC(voidType);
            createMethodPrintI(voidType);
            createMethodPrintS(voidType);
            createMethodPrintln(voidType);
            createMethodPrintBln(voidType);
            createMethodPrintCln(voidType);
            createMethodPrintIln(voidType);
            createMethodPrintSln(voidType);
        }catch(SemanticException e) {
            //This catch block souldn't be reachable
        }
    }

    private void createMethodPrintSln(Type voidType) throws SemanticException {
        addMethod( new MethodEntry(new TokenImpl(Token.TokenType.METVARID, "printSln", -1)));
        getCurrentMethod().setReturnType(voidType);
        setStaticModifier();
        
        Type stringType = new ClassType(new TokenImpl(Token.TokenType.CLASSID, "String", -1), null);
        getCurrentMethod().addParameter(new ParameterEntry(stringType, new TokenImpl(Token.TokenType.METVARID, "s", -1)));
        
    }

    private void createMethodPrintIln(Type voidType) throws SemanticException {
        addMethod(new MethodEntry(new TokenImpl(Token.TokenType.METVARID, "printIln", -1)));
        getCurrentMethod().setReturnType(voidType);
        setStaticModifier();
        
        Type intType = new PrimitiveType(new TokenImpl(Token.TokenType.INT_WORD, "int", -1));
        getCurrentMethod().addParameter(new ParameterEntry(intType, new TokenImpl(Token.TokenType.METVARID, "i", -1)));
        
    }

    private void createMethodPrintCln(Type voidType) throws SemanticException  {
        addMethod(new MethodEntry(new TokenImpl(Token.TokenType.METVARID, "printCln", -1)));
        getCurrentMethod().setReturnType(voidType);
        setStaticModifier();
        
        Type charType = new PrimitiveType(new TokenImpl(Token.TokenType.CHAR_WORD, "char", -1));
        getCurrentMethod().addParameter(new ParameterEntry(charType, new TokenImpl(Token.TokenType.METVARID, "c", -1)));
        
    }

    private void createMethodPrintBln(Type voidType) throws SemanticException  {
        addMethod(new MethodEntry(new TokenImpl(Token.TokenType.METVARID, "printBln", -1)));
        getCurrentMethod().setReturnType(voidType);
        setStaticModifier();
        
        Type booleanType = new PrimitiveType(new TokenImpl(Token.TokenType.BOOLEAN_WORD, "boolean", -1));
        getCurrentMethod().addParameter(new ParameterEntry(booleanType, new TokenImpl(Token.TokenType.METVARID, "b", -1)));
        
    }

    private void createMethodPrintln(Type voidType) throws SemanticException  {
        addMethod(new MethodEntry(new TokenImpl(Token.TokenType.METVARID, "println", -1)));
        getCurrentMethod().setReturnType(voidType);
        setStaticModifier();
        
        
    }

    private void createMethodPrintS(Type voidType) throws SemanticException  {
        addMethod(new MethodEntry(new TokenImpl(Token.TokenType.METVARID, "printS", -1)));
        getCurrentMethod().setReturnType(voidType);
        setStaticModifier();
        
        Type stringType = new ClassType(new TokenImpl(Token.TokenType.CLASSID, "String", -1), null);
        getCurrentMethod().addParameter(new ParameterEntry(stringType, new TokenImpl(Token.TokenType.METVARID, "s", -1)));
        
    }

    private void createMethodPrintI(Type voidType) throws SemanticException  {
        addMethod(new MethodEntry(new TokenImpl(Token.TokenType.METVARID, "printI", -1)));
        getCurrentMethod().setReturnType(voidType);
        setStaticModifier();
        
        Type intType = new PrimitiveType(new TokenImpl(Token.TokenType.INT_WORD, "int", -1));
        getCurrentMethod().addParameter(new ParameterEntry(intType, new TokenImpl(Token.TokenType.METVARID, "i", -1)));
        
    }

    private void createMethodPrintC(Type voidType) throws SemanticException  {
        addMethod(new MethodEntry(new TokenImpl(Token.TokenType.METVARID, "printC", -1)));
        getCurrentMethod().setReturnType(voidType);
        setStaticModifier();
        
        Type charType = new PrimitiveType(new TokenImpl(Token.TokenType.CHAR_WORD, "char", -1));
        getCurrentMethod().addParameter(new ParameterEntry(charType, new TokenImpl(Token.TokenType.METVARID, "c", -1)));
        
    }

    private void createMethodPrintB(Type voidType) throws SemanticException  {
        addMethod(new MethodEntry(new TokenImpl(Token.TokenType.METVARID, "printB", -1)));
        getCurrentMethod().setReturnType(voidType);
        setStaticModifier();
        
        Type booleanType = new PrimitiveType(new TokenImpl(Token.TokenType.BOOLEAN_WORD, "boolean", -1));
        getCurrentMethod().addParameter(new ParameterEntry(booleanType, new TokenImpl(Token.TokenType.METVARID, "b", -1)));
        
    }

    private void createMethodRead() throws SemanticException  {
        addMethod(new MethodEntry(new TokenImpl(Token.TokenType.METVARID, "read", -1)));
        Type intType = new PrimitiveType(new TokenImpl(Token.TokenType.INT_WORD, "int", -1));
        getCurrentMethod().setReturnType(intType);
        setStaticModifier();
        
        
    }
    
    private void setStaticModifier(){
        Token staticToken = new TokenImpl(Token.TokenType.STATIC_WORD, "static", -1);
        try{
            getCurrentMethod().setModifier(staticToken);
        }catch(Exception e){
            //This catch block shouldn't be reachable
        }
    }
}
