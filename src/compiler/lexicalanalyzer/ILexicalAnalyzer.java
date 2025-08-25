package compiler.lexicalanalyzer;

import compiler.IToken;

public interface ILexicalAnalyzer {
    public IToken nextToken();
}
