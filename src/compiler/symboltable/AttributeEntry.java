package compiler.symboltable;

import compiler.exceptions.SemanticException;
import compiler.token.Token;
import compiler.token.TokenImpl;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class AttributeEntry {
    private final Token token;
    String name;
    Type type;
    Token visibility;

    public AttributeEntry(Token token, Type type) {
        this.token = token;
        this.name = symbolTable.getCurrentClass().getName() + token.getLexeme();
        this.type = type;
        this.visibility = new TokenImpl(Token.TokenType.PUBLIC_WORD, "public", -1);
    }

    public void setVisibility(Token visibility) {
        this.visibility = visibility;
    }

    public String getName() {
        return name;
    }

    public void checkDeclaration() throws SemanticException {
        if(type.getToken().getTokenType() == Token.TokenType.CLASSID
         && ! symbolTable.existsClass(type.getName()))
            throw new SemanticException("Class not found", type.getToken());
    }

    public String print() {
        return name + " : " + type.getName() + " " + visibility.getLexeme();
    }

    public Token getToken() {
        return token;
    }
}
