package compiler.exceptions;

public class UnclosedStringException extends LexicalException{
    private static final String message = " -> String literal isn't closed";

    public UnclosedStringException(String lexemeException, int lineNumber, int columnNumber, String line) {
        super(message, lexemeException, lineNumber, columnNumber, line);
    }
}
