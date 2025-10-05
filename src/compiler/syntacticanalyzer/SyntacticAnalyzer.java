package compiler.syntacticanalyzer;

import compiler.exceptions.LexicalException;
import compiler.exceptions.SyntacticExceptions;

public interface SyntacticAnalyzer {

    void start() throws LexicalException, SyntacticExceptions;
}
