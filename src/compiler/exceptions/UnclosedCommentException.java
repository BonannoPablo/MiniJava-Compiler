package compiler.exceptions;

public class UnclosedCommentException extends LexicalException{
    private static final String message = "Comment isn't closed";

    public UnclosedCommentException(String lexemeException, int lineNumber, int columnNumber, String line) {
        super(message, lexemeException, lineNumber, columnNumber, line);
    }
}
