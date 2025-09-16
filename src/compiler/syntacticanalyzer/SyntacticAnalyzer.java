package compiler.syntacticanalyzer;

import compiler.exceptions.LexicalException;
import compiler.lexicalanalyzer.ILexicalAnalyzer;
import compiler.token.IToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SyntacticAnalyzer{
    private Map<String, Set<IToken.TokenType>> firstMap;
    private ILexicalAnalyzer lexicalAnalyzer;
    private IToken currentToken;

    public SyntacticAnalyzer(ILexicalAnalyzer lexicalAnalyzer) throws LexicalException {
        this.lexicalAnalyzer = lexicalAnalyzer;
        LoadFirstMap();
        retrieveNextToken();
    }

    public void start() throws LexicalException{
        classList();
        match(IToken.TokenType.EOF);
    }

    private void classList() throws LexicalException{
        if(firstMap.get("classList").contains(currentToken.getTokenType())){
            classNT();
            classList();
        } else {
            match(IToken.TokenType.EOF);
        }
    }

    private void classNT() throws LexicalException {
        optionalModifier();
        match(IToken.TokenType.CLASS_WORD);
        match(IToken.TokenType.CLASSID);
        optionalInheritance();
        match(IToken.TokenType.OPENING_BRACE);
        memberList();
        match(IToken.TokenType.CLOSING_BRACE);
    }

    private void modifier() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.ABSTRACT_WORD:
            case IToken.TokenType.FINAL_WORD:
            case IToken.TokenType.STATIC_WORD:
                    retrieveNextToken();
                    break;
            default:
                //TODO throw exception
        }
    }

    private void optionalModifier() throws LexicalException {
        if(firstMap.get("optionalModifier").contains(currentToken.getTokenType())){
            modifier();
        } else {
            //TODO throw exception
        }
    }

    private void optionalInheritance() throws LexicalException {
        if(currentToken.getTokenType().equals(IToken.TokenType.EXTENDS_WORD)) {
            retrieveNextToken();
            match(IToken.TokenType.CLASSID);
        }
    }

    private void memberList(){
        if(firstMap.get("memberList").contains(currentToken.getTokenType())) {
            member();
            memberList();
        }
    }

    private void member() throws LexicalException {
        if(currentToken.getTokenType().equals(IToken.TokenType.VOID_WORD)) {
          method();
        }
        else if(firstMap.get("type").contains(currentToken.getTokenType())) {
            type();
            match(IToken.TokenType.METVARID);
            attributeMethod();
        }
        else if(firstMap.get("modifier").contains(currentToken.getTokenType())) {
            modifier();
            methodType();
            method();
        } else if(firstMap.get("constructor").contains(currentToken.getTokenType())){
            constructor();
        }
    }

    private void attributeMethod() throws LexicalException {
        if(firstMap.get("formalArgs").contains(currentToken.getTokenType())) {
            formalArgs();
            optionalBlock();
        } else{
            match(IToken.TokenType.SEMICOLON);
        }
    }

    private void method(){
        match(IToken.TokenType.METVARID);
        formalArgs();
        optionalBlock();
    }

    private void constructor(){
        match(IToken.TokenType.PUBLIC_WORD);
        match(IToken.TokenType.CLASSID);
        formalArgs();
        block();
    }

    private void methodType() throws LexicalException {
        if(currentToken.equals(IToken.TokenType.VOID_WORD)) {
            retrieveNextToken();
        } else{
            type();
        }
    }

    private void type() throws LexicalException {
        if(currentToken.getTokenType().equals(IToken.TokenType.CLASSID)) {
            retrieveNextToken();
        } else {
            primitiveType();
        }
    }

    private void primitiveType() throws LexicalException {
        switch (currentToken.getTokenType()) {
            case IToken.TokenType.INT_WORD:
            case IToken.TokenType.BOOLEAN_WORD:
            case IToken.TokenType.CHAR_WORD:
                retrieveNextToken();
                break;
            default:
                //TODO throw exception
        }
    }

    private void formalArgs(){
        match(IToken.TokenType.OPENING_PAREN);
        optionalFormalArgsList();
        match(IToken.TokenType.CLOSING_PAREN);
    }

    private void optionalFormalArgsList(){
        if()
    }

    private void match(IToken.TokenType tokenType) throws LexicalException{
        if (tokenType.equals(currentToken.getTokenType())) {
            retrieveNextToken();
        } else {
            //TODO throw exception
        }
    }

    private void retrieveNextToken()throws LexicalException{
        currentToken = lexicalAnalyzer.nextToken();
    }

    private void LoadFirstMap() {
        firstMap = new HashMap<>();

    }


}
