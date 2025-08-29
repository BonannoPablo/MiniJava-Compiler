package compiler.lexicalanalyzer;

import compiler.exceptions.*;
import compiler.token.IToken;

public interface ILexicalAnalyzer {
    public IToken nextToken() throws LexicalException;
}
