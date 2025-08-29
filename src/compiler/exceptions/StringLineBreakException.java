package compiler.exceptions;

public class StringLineBreakException extends LexicalException {
    private static final String message = " -> Line breaks are not allowed in string literals";

    public StringLineBreakException(String lexeme, int lineNumber, int columnNumber) {
        super(message, lexeme, lineNumber, columnNumber);
    }
}
