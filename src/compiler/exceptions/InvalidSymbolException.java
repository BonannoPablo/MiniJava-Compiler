package compiler.exceptions;

public class InvalidSymbolException extends LexicalException{
    private static final String message = " isn't a valid symbol";
    public InvalidSymbolException(String lexemeException, int lineNumber) {
        super(message, lexemeException, lineNumber);
    }

    public InvalidSymbolException(String lexemeException, int lineNumber, int columnNumber) {
        super(message, lexemeException, lineNumber, columnNumber);
    }
}
