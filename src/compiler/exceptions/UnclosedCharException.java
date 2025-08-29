package compiler.exceptions;

public class UnclosedCharException extends LexicalException{
    private static final String message = " -> Char literal isn't closed";

    public UnclosedCharException(String lexemeException, int lineNumber, int columnNumber) {
        super(message, lexemeException, lineNumber, columnNumber);
    }
}
