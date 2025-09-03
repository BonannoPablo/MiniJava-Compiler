package compiler.exceptions;

public class InvalidSymbolException extends LexicalException{
    private static final String message = " isn't a valid symbol";

    public InvalidSymbolException(String lexemeException, int lineNumber, int columnNumber, String line) {
        super(message, lexemeException, lineNumber, columnNumber, line);
    }
}
