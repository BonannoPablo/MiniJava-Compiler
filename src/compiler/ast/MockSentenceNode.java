package compiler.ast;

public class MockSentenceNode extends SentenceNode{

    @Override
    public void print(int level) {
        System.out.println(" ".repeat(level) + "MOCK SENTENCE");
    }
}
