package compiler.exceptions;

public class IllegalUnicodeException extends LexicalException{
    private static final String message = " isn't a valid unicode character";

    public IllegalUnicodeException(String lexemeException, int lineNumber, int columnNumber, String line) {
        super(message, lexemeException, lineNumber, columnNumber, line);
    }
}
