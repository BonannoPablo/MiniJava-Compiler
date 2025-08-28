package compiler.exceptions;

public class IntLiteralLengthException extends LexicalException{
    private static final String message = " int literal can't be longer than 9 digits";
    public IntLiteralLengthException(String lexemeException, int lineNumber, int columnNumber) {
        super(message, lexemeException, lineNumber, columnNumber);
    }

    public IntLiteralLengthException(String lexemeException, int lineNumber) {
        super(message, lexemeException, lineNumber);
    }
}
