package compiler.exceptions;

public abstract class  LexicalException extends Exception{
    private final String lexeme;
    private final int lineNumber;

    public LexicalException(String message, String lexeme, int lineNumber){
        super(message);
        this.lexeme = lexeme;
        this.lineNumber = lineNumber;
    }

    public LexicalException(String message, String lexeme, int lineNumber, int columnNumber){
        super(message);
        this.lexeme = lexeme;
        this.lineNumber = lineNumber;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
