package compiler.ast;

import compiler.exceptions.SemanticException;
import compiler.symboltable.SymbolTable;
import compiler.token.Token;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class BlockNode extends SentenceNode{
    protected List<SentenceNode> sentences;
    protected Map<String, Token> localVariables;
    protected BlockNode parent;

    public BlockNode(){
        sentences = new LinkedList<>();
        localVariables = new HashMap<>();
    }

    public void addSentence(SentenceNode sentence){
        sentences.addLast(sentence);
    }

    public void print(int level){
        System.out.println(" ".repeat(level)+"BLOCK");
        for(var sentence : sentences)
            sentence.print(level+1);
    }

    public void addLocalVar(Token token) throws SemanticException {
        if(containsLocalVar(token.getLexeme()))
            throw new SemanticException("Variable is already defined", token);

        var entry = localVariables.put(token.getLexeme(), token);
        if (entry != null){
            throw new SemanticException("Variable is already defined", token);
        };
    }

    public boolean containsLocalVar(String name){
        return localVariables.containsKey(name) || (parent != null && parent.containsLocalVar(name))
                || symbolTable.getCurrentClass().getCurrentMethod().hasParameter(name);
    }

    public void setParent(BlockNode parent){
        this.parent = parent;
    }

    public BlockNode getParent(){
        return parent;
    }
}
