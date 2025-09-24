package compiler.exceptions;

import java.util.LinkedList;
import java.util.Queue;

public class SyntacticExceptions extends Exception {
    private final Queue<SyntacticException> queue;
    public SyntacticExceptions(Queue<SyntacticException> queue) {
        this.queue = queue;
    }

    public Queue<SyntacticException> getExceptionsQueue() {
        return queue;
    }
}
