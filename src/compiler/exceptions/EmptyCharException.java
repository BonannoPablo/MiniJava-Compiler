package compiler.exceptions;

public class EmptyCharException extends LexicalException{
    private static final String message = " -> Char literal can't be empty";

    public EmptyCharException(String lexemeException, int lineNumber, int columnNumber, String line) {
        super(message, lexemeException, lineNumber, columnNumber, line);
    }
}
