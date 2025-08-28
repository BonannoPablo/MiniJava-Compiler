package compiler.lexicalanalyzer;

import compiler.token.IToken;
import compiler.token.Token;
import sourcemanager.SourceManager;

import java.io.IOException;

public class LexicalAnalyzerWithCases implements ILexicalAnalyzer{
    private enum States{
        START_STATE,
        INT_STATE,
        CLASS_STATE,
        METVAR_STATE,
        LESS_THAN_STATE,
        GREATER_THAN_STATE,
        EXCLAMATION_POINT_STATE,
        EQUAL_STATE,
        AMPERSAND_STATE,
        PIPE_STATE,
        PLUS_STATE,
        SUBTRACT_STATE,
        SLASH_STATE,
        LINE_COMMENT_STATE,
        COMMENT_STATE,
        COMMENT_END_STATE,
        CHAR_LITERAL_STATE,
        SPECIAL_CHAR_STATE,
        CHAR_LITERAL_END_STATE,
        UNICODE_CHAR_STATE,
        STRING_LITERAL_STATE,
        STRING_SPECIAL_CHAR_STATE,
    }

    String lexeme;
    char currentChar;
    SourceManager sourceManager;
    States currentState;

    public LexicalAnalyzerWithCases(SourceManager sourceManager) {
        this.sourceManager = sourceManager;
        retrieveNextChar();
        lexeme = null;
    }

    @Override
    public IToken nextToken() {
        restartLexeme();
        currentState = States.START_STATE;
        while(true){
            switch (currentState){
                case START_STATE:
                    if(Character.isWhitespace(currentChar)){
                        retrieveNextChar();
                        break;
                    }
                    else if(Character.isDigit(currentChar)){
                        currentState = States.INT_STATE;
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    }
                    else if(Character.isUpperCase(currentChar)){
                        currentState = States.CLASS_STATE;
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    }
                    else if(Character.isLowerCase(currentChar)) {
                        currentState = States.METVAR_STATE;
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    }
                    else {
                        switch (currentChar) {
                            case '(':
                                retrieveNextChar();
                                return new Token(IToken.TokenType.OPENING_PAREN, "(", sourceManager.getLineNumber());
                            case ')':
                                retrieveNextChar();
                                return new Token(IToken.TokenType.CLOSING_PAREN, ")", sourceManager.getLineNumber());
                            case '{':
                                retrieveNextChar();
                                return new Token(IToken.TokenType.OPENING_BRACE, "{", sourceManager.getLineNumber());
                            case '}':
                                retrieveNextChar();
                                return new Token(IToken.TokenType.CLOSING_BRACE, "}", sourceManager.getLineNumber());
                            case ';':
                                retrieveNextChar();
                                return new Token(IToken.TokenType.SEMICOLON, ";", sourceManager.getLineNumber());
                            case ',':
                                retrieveNextChar();
                                return new Token(IToken.TokenType.COMMA, ",", sourceManager.getLineNumber());
                            case '.':
                                retrieveNextChar();
                                return new Token(IToken.TokenType.PERIOD, ".", sourceManager.getLineNumber());
                            case ':':
                                retrieveNextChar();
                                return new Token(IToken.TokenType.COLON, ":", sourceManager.getLineNumber());
                            case '<':
                                updateLexeme();
                                currentState = States.LESS_THAN_STATE;
                                retrieveNextChar();
                                break;
                            case '>':
                                updateLexeme();
                                currentState = States.GREATER_THAN_STATE;
                                retrieveNextChar();
                                break;
                            case '!':
                                updateLexeme();
                                currentState = States.EXCLAMATION_POINT_STATE;
                                retrieveNextChar();
                                break;
                            case '=':
                                updateLexeme();
                                currentState = States.EQUAL_STATE;
                                retrieveNextChar();
                                break;
                            case '&':
                                updateLexeme();
                                currentState = States.AMPERSAND_STATE;
                                retrieveNextChar();
                                break;
                            case '|':
                                updateLexeme();
                                currentState = States.PIPE_STATE;
                                retrieveNextChar();
                                break;
                            case '%':
                                retrieveNextChar();
                                return new Token(IToken.TokenType.PERCENT, "%", sourceManager.getLineNumber());
                            case '+':
                                updateLexeme();
                                currentState = States.PLUS_STATE;
                                retrieveNextChar();
                                break;
                            case '-':
                                updateLexeme();
                                currentState = States.SUBTRACT_STATE;
                                retrieveNextChar();
                                break;
                            case '*':
                                retrieveNextChar();
                                return new Token(IToken.TokenType.MULTIPLY, "*", sourceManager.getLineNumber());
                            case '/':
                                updateLexeme();
                                currentState = States.SLASH_STATE;
                                retrieveNextChar();
                                break;
                            case '\'':
                                updateLexeme();
                                currentState = States.CHAR_LITERAL_STATE;
                                retrieveNextChar();
                                break;
                            case '"':
                                updateLexeme();
                                currentState = States.STRING_LITERAL_STATE;
                                retrieveNextChar();
                                break;
                            case SourceManager.END_OF_FILE:
                                return new Token(IToken.TokenType.EOF, "EOF", sourceManager.getLineNumber());
                            default:
                                //TODO throw exception
                        }
                    }
                break;
                case INT_STATE:
                    if(Character.isDigit(currentChar)){
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    }
                    else if (lexeme.length() > 9){
                        //TODO throw exception int too long
                    }
                    else{
                        return new Token(IToken.TokenType.INTLITERAL, lexeme, sourceManager.getLineNumber());
                    }
                case CLASS_STATE:
                    if(Character.isLetter(currentChar) || currentChar == '_' || Character.isDigit(currentChar)){
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    }
                    else{
                        return new Token(IToken.TokenType.CLASSID, lexeme, sourceManager.getLineNumber());
                    }
                case METVAR_STATE:
                    if(Character.isLetter(currentChar) || currentChar == '_' || Character.isDigit(currentChar)){
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    }
                    else{
                        return checkKeyWords();
                    }
                case LESS_THAN_STATE:
                    if(currentChar == '='){
                        retrieveNextChar();
                        return new Token(IToken.TokenType.EQUAL_LESS_THAN, "<=", sourceManager.getLineNumber());
                    }
                    else{
                        return new Token(IToken.TokenType.LESS_THAN, "<", sourceManager.getLineNumber());
                    }
                case GREATER_THAN_STATE:
                    if(currentChar == '='){
                        retrieveNextChar();
                        return new Token(IToken.TokenType.EQUAL_GREATER_THAN, ">=", sourceManager.getLineNumber());
                    }
                    else{
                        return new Token(IToken.TokenType.GREATER_THAN, ">", sourceManager.getLineNumber());
                    }
                case EXCLAMATION_POINT_STATE:
                    if(currentChar == '='){
                        retrieveNextChar();
                        return new Token(IToken.TokenType.DIFERENT, "!=", sourceManager.getLineNumber());
                    }
                    else{
                        return new Token(IToken.TokenType.EXCLAMATION_POINT, "!", sourceManager.getLineNumber());
                    }
                case EQUAL_STATE:
                    if(currentChar == '='){
                        retrieveNextChar();
                        return new Token(IToken.TokenType.EQUALS_COMPARISON, "==", sourceManager.getLineNumber());
                    }
                    else{
                        return new Token(IToken.TokenType.EQUAL, "=", sourceManager.getLineNumber());
                    }
                case AMPERSAND_STATE:
                    if(currentChar == '&'){
                        retrieveNextChar();
                        return new Token(IToken.TokenType.AND, "&&", sourceManager.getLineNumber());
                    }
                    else{
                        //TODO throw exception
                    }
                case PIPE_STATE:
                    if(currentChar == '|'){
                        retrieveNextChar();
                        return new Token(IToken.TokenType.OR, "||", sourceManager.getLineNumber());
                    }
                    else{
                        //TODO throw exception
                    }
                case PLUS_STATE:
                    if(currentChar == '+'){
                        retrieveNextChar();
                        return new Token(IToken.TokenType.PLUS1, "++", sourceManager.getLineNumber());
                    }
                    else{
                        return new Token(IToken.TokenType.PLUS, "+", sourceManager.getLineNumber());
                    }
                case SUBTRACT_STATE:
                    if(currentChar == '-'){
                        retrieveNextChar();
                        return new Token(IToken.TokenType.MINUS1, "--", sourceManager.getLineNumber());
                    }
                    else {
                        return new Token(IToken.TokenType.MINUS, "-", sourceManager.getLineNumber());
                    }
                case SLASH_STATE:
                    switch (currentChar) {
                        case '*':
                            retrieveNextChar();
                            currentState = States.COMMENT_STATE;
                            break;
                        case '/':
                            retrieveNextChar();
                            currentState = States.LINE_COMMENT_STATE;
                            break;
                        default:
                            return new Token(IToken.TokenType.SLASH, "/", sourceManager.getLineNumber());
                    }
                    break;
                case LINE_COMMENT_STATE:
                    if(currentChar == '\n'){
                        currentState = States.START_STATE;
                        restartLexeme();
                    }
                    retrieveNextChar();
                    break;
                case COMMENT_STATE:
                    switch (currentChar) {
                        case '*':
                            currentState = States.COMMENT_END_STATE;
                            retrieveNextChar();
                            break;
                        case SourceManager.END_OF_FILE:
                            //TODO throw exception
                        default:
                            retrieveNextChar();
                    }
                    break;
                case COMMENT_END_STATE:
                    switch (currentChar) {
                        case '/':
                            currentState = States.START_STATE;
                            restartLexeme();
                            retrieveNextChar();
                            break;
                        case SourceManager.END_OF_FILE:
                            //TODO throw exception
                        default:
                            retrieveNextChar();
                    }
                    break;
                    case CHAR_LITERAL_STATE:
                        switch (currentChar) {
                            case '\'':
                                //TODO throw exception empty char
                            case '\\':
                                updateLexeme();
                                currentState = States.SPECIAL_CHAR_STATE;
                                retrieveNextChar();
                                break;
                            case SourceManager.END_OF_FILE:
                            case '\n':
                                //TODO throw exception unclosed char
                            default:
                                updateLexeme();
                                currentState = States.CHAR_LITERAL_END_STATE;
                                retrieveNextChar();
                                break;
                        }
                        break;
                    case SPECIAL_CHAR_STATE:
                        switch (currentChar) {
                            case '\n':
                            case SourceManager.END_OF_FILE:
                                //TODO throw exception unclosed char
                            case 'u':
                                updateLexeme();
                                currentState = States.UNICODE_CHAR_STATE;
                                retrieveNextChar();
                                break;
                            default:
                                updateLexeme();
                                currentState = States.CHAR_LITERAL_END_STATE;
                                retrieveNextChar();
                        }
                        break;
                    case UNICODE_CHAR_STATE:
                        if(Character.isDigit(currentChar)){
                            updateLexeme();
                            retrieveNextChar();
                        } else if (currentChar == '\''){
                            if(lexeme.length() <= 6){
                                updateLexeme();
                                retrieveNextChar();
                                return new Token(IToken.TokenType.CHARLITERAL, lexeme, sourceManager.getLineNumber());
                            } else {
                                //TODO throw exception too many characters in char literal
                            }
                        } else{
                            //TODO throw exception illegal unicode escape sequence
                        }
                        break;
                    case CHAR_LITERAL_END_STATE:
                        if(currentChar == '\''){
                            updateLexeme();
                            retrieveNextChar();
                            return new Token(IToken.TokenType.CHARLITERAL, lexeme, sourceManager.getLineNumber());
                        }
                        else if(currentChar == SourceManager.END_OF_FILE){
                            //TODO throw exception
                        }
                        else{
                            //TODO throw exception
                        }
                    case STRING_LITERAL_STATE:
                        switch (currentChar) {
                            case '"':
                                updateLexeme();
                                retrieveNextChar();
                                return new Token(IToken.TokenType.STRINGLITERAL, lexeme, sourceManager.getLineNumber());
                            case SourceManager.END_OF_FILE:
                            case '\n':
                                //TODO throw exception unclosed string
                            case '\\':
                                updateLexeme();
                                currentState = States.STRING_SPECIAL_CHAR_STATE;
                                retrieveNextChar();
                                break;
                            default:
                                updateLexeme();
                                retrieveNextChar();
                                break;
                        }
                        break;
                    case STRING_SPECIAL_CHAR_STATE:
                        if(currentChar == SourceManager.END_OF_FILE || currentChar == '\n'){
                            //TODO throw exception unclosed string
                        }
                        else{
                            updateLexeme();
                            currentState = States.STRING_LITERAL_STATE;
                            retrieveNextChar();
                        }
                        break;

            }
        }
    }

    private Token checkKeyWords() {
        IToken.TokenType type;
        type = switch (lexeme) {
            case "class" -> IToken.TokenType.CLASS_WORD;
            case "extends" -> IToken.TokenType.EXTENDS_WORD;
            case "public" -> IToken.TokenType.PUBLIC_WORD;
            case "static" -> IToken.TokenType.STATIC_WORD;
            case "void" -> IToken.TokenType.VOID_WORD;
            case "boolean" -> IToken.TokenType.BOOLEAN_WORD;
            case "char" -> IToken.TokenType.CHAR_WORD;
            case "int" -> IToken.TokenType.INT_WORD;
            case "abstract" -> IToken.TokenType.ABSTRACT_WORD;
            case "final" -> IToken.TokenType.FINAL_WORD;
            case "if" -> IToken.TokenType.IF_WORD;
            case "else" -> IToken.TokenType.ELSE_WORD;
            case "while" -> IToken.TokenType.WHILE_WORD;
            case "return" -> IToken.TokenType.RETURN_WORD;
            case "var" -> IToken.TokenType.VAR_WORD;
            case "this" -> IToken.TokenType.THIS_WORD;
            case "new" -> IToken.TokenType.NEW_WORD;
            case "null" -> IToken.TokenType.NULL_WORD;
            case "true" -> IToken.TokenType.TRUE_WORD;
            case "false" -> IToken.TokenType.FALSE_WORD;
            default -> IToken.TokenType.METVARID;
        };
        return new Token(type, lexeme, sourceManager.getLineNumber());
    }

    private void restartLexeme() {
        lexeme = "";
    }

    private void updateLexeme() {
        lexeme += currentChar;
    }

    private void retrieveNextChar() {
        try {
            currentChar = sourceManager.getNextChar();
        } catch (IOException e) {
            throw new RuntimeException(e); //TODO handle exception
        }
    }
}
