package compiler.exceptions;

public abstract class  LexicalException extends Exception{
    private final String lexeme;
    private final int lineNumber;
    private final int columnNumber;

    public LexicalException(String message, String lexeme, int lineNumber, int columnNumber){
        super(message);
        this.lexeme = lexeme;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}
