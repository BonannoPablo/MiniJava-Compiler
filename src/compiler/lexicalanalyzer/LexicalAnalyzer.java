package compiler.lexicalanalyzer;

import compiler.exceptions.*;
import compiler.token.Token;

public interface LexicalAnalyzer {
    public Token nextToken() throws LexicalException;
}
