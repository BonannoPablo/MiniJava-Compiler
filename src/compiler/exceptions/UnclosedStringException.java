package compiler.exceptions;

public class UnclosedStringException extends LexicalException{
    private static final String message = " -> String literal isn't closed";

    public UnclosedStringException(String lexemeException, int lineNumber, int columnNumber) {
        super(message, lexemeException, lineNumber, columnNumber);
    }
}
