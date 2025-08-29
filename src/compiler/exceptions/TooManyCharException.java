package compiler.exceptions;

public class TooManyCharException extends LexicalException{
    private static final String message = " char literal can't be longer than 1 character";

    public TooManyCharException(String lexemeException, int lineNumber, int columnNumber) {
        super(message, lexemeException, lineNumber, columnNumber);
    }
}
