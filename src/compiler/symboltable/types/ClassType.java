package compiler.symboltable.types;

import compiler.token.Token;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class ClassType extends Type{
    Token genericType;

    public ClassType(Token token, Token genericType) {
        super(token);
        this.genericType = genericType;
    }

    public ClassType(String name) {
        super(name);
    }

    public String getGenericType() {
        return genericType != null ? genericType.getLexeme() : "";
    }

    @Override
    public Token getGenericTypeToken() {
        return genericType;
    }

    @Override
    public boolean conforms(Type t){
        if(t.getName().equals(this.getName()) || t.getName().equals("Object"))
            return true;
        var classEntry = symbolTable.getClassEntry(getName());
        var found = false;
        if(t instanceof ClassType)
            while(!classEntry.getParent().getLexeme().equals("Object") && ! found)
                classEntry = symbolTable.getClassEntry(classEntry.getParent().getLexeme());
        return found;
    }
}
