package compiler.exceptions;

public class TooManyCharException extends LexicalException{
    private static final String message = " -> Too many characters in char literal";

    public TooManyCharException(String lexemeException, int lineNumber, int columnNumber, String line) {
        super(message, lexemeException, lineNumber, columnNumber, line);
    }
}
