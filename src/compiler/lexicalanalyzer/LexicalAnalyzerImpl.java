package compiler.lexicalanalyzer;

import compiler.exceptions.*;
import compiler.token.Token;
import compiler.token.TokenImpl;
import sourcemanager.SourceManager;

import java.io.IOException;

public class LexicalAnalyzerImpl implements LexicalAnalyzer {
    private enum States {
        START_STATE,
        INT_STATE,
        CLASS_STATE,
        METVAR_STATE,
        LESS_THAN_STATE,
        GREATER_THAN_STATE,
        EXCLAMATION_POINT_STATE,
        EQUAL_STATE,
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
        STRING_SPECIAL_CHAR_STATE, UNICODE_CODE_POINT_CHAR_STATE,
    }

    private String lexeme;
    private char currentChar;
    private final SourceManager sourceManager;
    States currentState;

    public LexicalAnalyzerImpl(SourceManager sourceManager) {
        this.sourceManager = sourceManager;
        retrieveNextChar();
        currentState = States.START_STATE;
        lexeme = null;
    }

    @Override
    public Token nextToken() throws LexicalException {
        restartLexeme();
        int unicodeCodePointDigits = 0;

        currentState = States.START_STATE;

        while (true) {
            switch (currentState) {
                case START_STATE:
                    if (Character.isWhitespace(currentChar)) {
                        retrieveNextChar();
                        break;
                    } else if (Character.isDigit(currentChar)) {
                        currentState = States.INT_STATE;
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    } else if (Character.isUpperCase(currentChar)) {
                        currentState = States.CLASS_STATE;
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    } else if (Character.isLowerCase(currentChar)) {
                        currentState = States.METVAR_STATE;
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    } else {
                        switch (currentChar) {
                            case '(':
                                retrieveNextChar();
                                return new TokenImpl(Token.TokenType.OPENING_PAREN, "(", sourceManager.getLineNumber());
                            case ')':
                                retrieveNextChar();
                                return new TokenImpl(Token.TokenType.CLOSING_PAREN, ")", sourceManager.getLineNumber());
                            case '{':
                                retrieveNextChar();
                                return new TokenImpl(Token.TokenType.OPENING_BRACE, "{", sourceManager.getLineNumber());
                            case '}':
                                retrieveNextChar();
                                return new TokenImpl(Token.TokenType.CLOSING_BRACE, "}", sourceManager.getLineNumber());
                            case ';':
                                retrieveNextChar();
                                return new TokenImpl(Token.TokenType.SEMICOLON, ";", sourceManager.getLineNumber());
                            case ',':
                                retrieveNextChar();
                                return new TokenImpl(Token.TokenType.COMMA, ",", sourceManager.getLineNumber());
                            case '.':
                                retrieveNextChar();
                                return new TokenImpl(Token.TokenType.PERIOD, ".", sourceManager.getLineNumber());
                            case ':':
                                retrieveNextChar();
                                return new TokenImpl(Token.TokenType.COLON, ":", sourceManager.getLineNumber());
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
                            case '?':
                                retrieveNextChar();
                                return new TokenImpl(Token.TokenType.QUESTION_MARK, "?", sourceManager.getLineNumber());
                            case '=':
                                updateLexeme();
                                currentState = States.EQUAL_STATE;
                                retrieveNextChar();
                                break;
                            case '&':
                                updateLexeme();
                                retrieveNextChar();
                                if (currentChar == '&') {
                                    retrieveNextChar();
                                    return new TokenImpl(Token.TokenType.AND, "&&", sourceManager.getLineNumber());
                                } else {
                                    throw new InvalidSymbolException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                                }
                            case '|':
                                updateLexeme();
                                retrieveNextChar();
                                if (currentChar == '|') {
                                    retrieveNextChar();
                                    return new TokenImpl(Token.TokenType.OR, "||", sourceManager.getLineNumber());
                                } else {
                                    throw new InvalidSymbolException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                                }
                            case '%':
                                retrieveNextChar();
                                return new TokenImpl(Token.TokenType.PERCENT, "%", sourceManager.getLineNumber());
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
                                return new TokenImpl(Token.TokenType.MULTIPLY, "*", sourceManager.getLineNumber());
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
                                return new TokenImpl(Token.TokenType.EOF, "", sourceManager.getLineNumber());
                            default:
                                updateLexeme();
                                retrieveNextChar();
                                throw new InvalidSymbolException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                        }
                    }
                    break;
                case INT_STATE:
                    if (Character.isDigit(currentChar)) {
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    } else if (lexeme.length() > 9) {
                        throw new IntLiteralLengthException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                    } else {
                        return new TokenImpl(Token.TokenType.INTLITERAL, lexeme, sourceManager.getLineNumber());
                    }
                case CLASS_STATE:
                    if (Character.isLetter(currentChar) || currentChar == '_' || Character.isDigit(currentChar)) {
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    } else {
                        return new TokenImpl(Token.TokenType.CLASSID, lexeme, sourceManager.getLineNumber());
                    }
                case METVAR_STATE:
                    if (Character.isLetter(currentChar) || currentChar == '_' || Character.isDigit(currentChar)) {
                        updateLexeme();
                        retrieveNextChar();
                        break;
                    } else {
                        return checkKeyWords();
                    }
                case LESS_THAN_STATE:
                    if (currentChar == '=') {
                        retrieveNextChar();
                        return new TokenImpl(Token.TokenType.EQUAL_LESS_THAN, "<=", sourceManager.getLineNumber());
                    } else {
                        return new TokenImpl(Token.TokenType.LESS_THAN, "<", sourceManager.getLineNumber());
                    }
                case GREATER_THAN_STATE:
                    if (currentChar == '=') {
                        retrieveNextChar();
                        return new TokenImpl(Token.TokenType.EQUAL_GREATER_THAN, ">=", sourceManager.getLineNumber());
                    } else {
                        return new TokenImpl(Token.TokenType.GREATER_THAN, ">", sourceManager.getLineNumber());
                    }
                case EXCLAMATION_POINT_STATE:
                    if (currentChar == '=') {
                        retrieveNextChar();
                        return new TokenImpl(Token.TokenType.DIFERENT, "!=", sourceManager.getLineNumber());
                    } else {
                        return new TokenImpl(Token.TokenType.EXCLAMATION_POINT, "!", sourceManager.getLineNumber());
                    }
                case EQUAL_STATE:
                    if (currentChar == '=') {
                        retrieveNextChar();
                        return new TokenImpl(Token.TokenType.EQUALS_COMPARISON, "==", sourceManager.getLineNumber());
                    } else {
                        return new TokenImpl(Token.TokenType.EQUAL, "=", sourceManager.getLineNumber());
                    }
                case PLUS_STATE:
                    if (currentChar == '+') {
                        retrieveNextChar();
                        return new TokenImpl(Token.TokenType.PLUS1, "++", sourceManager.getLineNumber());
                    } else {
                        return new TokenImpl(Token.TokenType.PLUS, "+", sourceManager.getLineNumber());
                    }
                case SUBTRACT_STATE:
                    if (currentChar == '-') {
                        retrieveNextChar();
                        return new TokenImpl(Token.TokenType.MINUS1, "--", sourceManager.getLineNumber());
                    } else {
                        return new TokenImpl(Token.TokenType.MINUS, "-", sourceManager.getLineNumber());
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
                            return new TokenImpl(Token.TokenType.SLASH, "/", sourceManager.getLineNumber());
                    }
                    break;
                case LINE_COMMENT_STATE:
                    if (currentChar == SourceManager.END_OF_FILE) {
                        return new TokenImpl(Token.TokenType.EOF, "", sourceManager.getLineNumber());
                    }
                    if (currentChar == '\n') {
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
                            throw new UnclosedCommentException("", sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
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
                            throw new UnclosedCommentException("", sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                        default:
                            retrieveNextChar();
                            currentState = States.COMMENT_STATE;
                    }
                    break;
                case CHAR_LITERAL_STATE:
                    switch (currentChar) {
                        case '\'':
                            updateLexeme();
                            retrieveNextChar();
                            throw new EmptyCharException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                        case '\\':
                            updateLexeme();
                            currentState = States.SPECIAL_CHAR_STATE;
                            retrieveNextChar();
                            break;
                        case SourceManager.END_OF_FILE:
                        case '\n':
                            throw new UnclosedCharException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
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
                            throw new UnclosedCharException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
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
                    if (currentChar == 'u') {
                        retrieveNextChar();
                        break;
                    }
                    if (Character.isDigit(currentChar) || 'a' <= currentChar && currentChar <= 'f' || 'A' <= currentChar && currentChar <= 'F') {
                        currentState = States.UNICODE_CODE_POINT_CHAR_STATE;
                        unicodeCodePointDigits++;
                        updateLexeme();
                        retrieveNextChar();
                    } else {
                        throw new IllegalUnicodeException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                    }
                    break;
                case UNICODE_CODE_POINT_CHAR_STATE:
                    if (Character.isDigit(currentChar) || 'a' <= currentChar && currentChar <= 'f' || 'A' <= currentChar && currentChar <= 'F') {
                        unicodeCodePointDigits++;
                        updateLexeme();
                        retrieveNextChar();
                    } else if (currentChar == '\'') {
                        if (unicodeCodePointDigits == 4) {
                            updateLexeme();
                            retrieveNextChar();
                            return new TokenImpl(Token.TokenType.CHARLITERAL, lexeme, sourceManager.getLineNumber());
                        } else  if (unicodeCodePointDigits > 4){
                            updateLexeme();
                            retrieveNextChar();
                            throw new TooManyCharException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                        } else{
                            updateLexeme();
                            retrieveNextChar();
                            throw new IllegalUnicodeException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                        }
                    } else {
                        throw new IllegalUnicodeException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                    }
                    break;
                case CHAR_LITERAL_END_STATE:
                    if (currentChar == '\'') {
                        updateLexeme();
                        retrieveNextChar();
                        return new TokenImpl(Token.TokenType.CHARLITERAL, lexeme, sourceManager.getLineNumber());
                    } else if (currentChar == SourceManager.END_OF_FILE || currentChar == '\n') {
                        throw new UnclosedCharException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                    } else {
                        retrieveNextChar();
                        throw new TooManyCharException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                    }
                case STRING_LITERAL_STATE:
                    switch (currentChar) {
                        case '"':
                            updateLexeme();
                            retrieveNextChar();
                            return new TokenImpl(Token.TokenType.STRINGLITERAL, lexeme, sourceManager.getLineNumber());
                        case SourceManager.END_OF_FILE:
                            throw new UnclosedStringException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                        case '\n':
                            throw new StringLineBreakException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
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
                    if (currentChar == SourceManager.END_OF_FILE) {
                        throw new UnclosedStringException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                    } else if (currentChar == '\n') {
                        throw new StringLineBreakException(lexeme, sourceManager.getLineNumber(), sourceManager.getLineIndexNumber(), getCurrentLine());
                    } else {
                        updateLexeme();
                        currentState = States.STRING_LITERAL_STATE;
                        retrieveNextChar();
                    }
                    break;

            }
        }
    }

    private String getCurrentLine() {
        String line = null;
        try{
            line = sourceManager.getLine();
        } catch(IOException e){
            System.out.println("There has been an error while reading the source file");
        }
        return line;
    }

    private TokenImpl checkKeyWords() {
        Token.TokenType type;
        type = switch (lexeme) {
            case "class" -> Token.TokenType.CLASS_WORD;
            case "interface" -> Token.TokenType.INTERFACE_WORD;
            case "extends" -> Token.TokenType.EXTENDS_WORD;
            case "implements" -> Token.TokenType.IMPLEMENTS_WORD;
            case "public" -> Token.TokenType.PUBLIC_WORD;
            case "private" -> Token.TokenType.PRIVATE_WORD;
            case "static" -> Token.TokenType.STATIC_WORD;
            case "void" -> Token.TokenType.VOID_WORD;
            case "boolean" -> Token.TokenType.BOOLEAN_WORD;
            case "char" -> Token.TokenType.CHAR_WORD;
            case "int" -> Token.TokenType.INT_WORD;
            case "abstract" -> Token.TokenType.ABSTRACT_WORD;
            case "final" -> Token.TokenType.FINAL_WORD;
            case "if" -> Token.TokenType.IF_WORD;
            case "else" -> Token.TokenType.ELSE_WORD;
            case "while" -> Token.TokenType.WHILE_WORD;
            case "for" -> Token.TokenType.FOR_WORD;
            case "return" -> Token.TokenType.RETURN_WORD;
            case "var" -> Token.TokenType.VAR_WORD;
            case "this" -> Token.TokenType.THIS_WORD;
            case "new" -> Token.TokenType.NEW_WORD;
            case "null" -> Token.TokenType.NULL_WORD;
            case "true" -> Token.TokenType.TRUE_WORD;
            case "false" -> Token.TokenType.FALSE_WORD;
            default -> Token.TokenType.METVARID;
        };
        return new TokenImpl(type, lexeme, sourceManager.getLineNumber());
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
            System.out.println("There has been an error while reading the source file");
        }
    }
}