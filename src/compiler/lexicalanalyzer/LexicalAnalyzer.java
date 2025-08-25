package compiler.lexicalanalyzer;

import compiler.IToken;
import compiler.Token;
import sourcemanager.SourceManager;

import java.io.IOException;


public class LexicalAnalyzer implements ILexicalAnalyzer {
    String tokenValue;
    char currentChar;
    SourceManager sourceManager;

    public LexicalAnalyzer(SourceManager sourceManager) {
        this.sourceManager = sourceManager;
        retrieveNextChar();
        tokenValue = null;
    }

    @Override
    public IToken nextToken() {
        restartTokenValue();

        return startingState();
    }


    private IToken startingState() {
        if(Character.isWhitespace(currentChar)){
            retrieveNextChar();
            return startingState();
        } else if(currentChar == SourceManager.END_OF_FILE){
            updateTokenValue();
            return new Token("EOF");
        }

        else {
            updateTokenValue();
            if (Character.isUpperCase(currentChar)){
                return classIdState();
            } else if (Character.isLowerCase(currentChar)) {
                return metVarIdState();
            } else if(Character.isDigit(currentChar)){
                return intLiteralState();
            } else if(currentChar == '\''){
                return charLiteralState1();
            } else if (currentChar == '"'){
                return stringLiteralState1();
            }
        }
        return null;
    }

    private IToken stringLiteralState1() {
        retrieveNextChar();
        if (currentChar == '"') { //TODO control valid chars
            updateTokenValue();
            retrieveNextChar();
            return new Token("stringLiteral");
        }
        if (currentChar == '\n') {
            new Exception("Line jump in String"); //TODO handle exception
        } else {
            updateTokenValue();
            return stringLiteralState1(); //TODO return type
        }
        return null;
    }


    private IToken classIdState() {
        retrieveNextChar();
        if(Character.isLetter(currentChar) || Character.isDigit(currentChar) || currentChar == '_'){
            updateTokenValue();
            return classIdState();
        }        else{
            return new Token("classId"); //TODO create Token
        }
    }

    private IToken metVarIdState() {
        retrieveNextChar();
        if(Character.isLetter(currentChar) || Character.isDigit(currentChar) || currentChar == '_'){
            updateTokenValue();
            return metVarIdState();
        }        else{
            return new Token("MetVarId"); //TODO create Token
        }
    }

    private IToken intLiteralState() {
        retrieveNextChar();
        if(Character.isDigit(currentChar)){
            updateTokenValue();
            return intLiteralState();
        } else if (tokenValue.length() > 9){
            new Exception("Integer too big"); //TODO handle exception
            System.out.print("Error: integer too long -- ");
        }
        return new Token("intLiteral");
    }

    private IToken charLiteralState1() {
        retrieveNextChar();
        if(currentChar == '\\'){
            updateTokenValue();
            return charLiteralState2();
        } else if (currentChar == '\'') {
            new Exception("empty char");
        } else {
            updateTokenValue();
            return charLiteralState3();
        }
        return null; //TODO return type
    }

    private IToken charLiteralState2() {
        retrieveNextChar();
        updateTokenValue();
        return charLiteralState3();
    }

    private IToken charLiteralState3() {
        retrieveNextChar();
        if(currentChar == '\''){
            retrieveNextChar();
            updateTokenValue();
        } else {
            new Exception("Invalid char"); //TODO handle exception
        }
        return new Token("charLiteral");
    }

    private void restartTokenValue() {
        tokenValue = "";
    }

    private void updateTokenValue() {
        tokenValue += currentChar;
    }

    private void retrieveNextChar() {
        try {
            currentChar = sourceManager.getNextChar();
        } catch (IOException e) {
            throw new RuntimeException(e); //TODO handle exception
        }
    }

}
