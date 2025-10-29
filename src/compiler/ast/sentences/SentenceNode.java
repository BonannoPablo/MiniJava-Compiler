package compiler.ast.sentences;

import compiler.exceptions.SemanticException;

public abstract class SentenceNode {
    public abstract void print(int level);
    public abstract void check() throws SemanticException;
}
