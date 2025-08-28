package compiler.lexicalanalyzer;

import compiler.token.IToken;

public interface ILexicalAnalyzer {
    public IToken nextToken();
}
