package compiler.ast.sentences;

import compiler.exceptions.SemanticException;

public class MockSentenceNode extends SentenceNode {

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level) + "MOCK SENTENCE");
    }

    @Override
    public void check() throws SemanticException {}
}
