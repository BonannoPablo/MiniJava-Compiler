package compiler.ast.sentences;

import compiler.exceptions.SemanticException;
import compiler.symboltable.ParameterEntry;
import compiler.token.Token;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static compiler.syntacticanalyzer.SyntacticAnalyzerImpl.symbolTable;

public class BlockNode extends SentenceNode {
    protected List<SentenceNode> sentences;
    protected Map<String, VarInitNode> localVariables;
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

    @Override
    public void check() throws SemanticException {
        symbolTable.setCurrentBlock(this);
        for(SentenceNode s : sentences){
            s.check();
        }
        symbolTable.setCurrentBlock(parent);
    }

    public void addLocalVar(VarInitNode var) throws SemanticException {
        if(containsLocalVar(var.getName()) || symbolTable.getCurrentClass().getCurrentMethod().hasParameter(var.getName()))
            throw new SemanticException("Variable is already defined", var.getToken());

        localVariables.put(var.getName(), var);
    }

    public boolean containsLocalVar(String name){
        return localVariables.containsKey(name) || (parent != null && parent.containsLocalVar(name));
    }

    public void setParent(BlockNode parent){
        this.parent = parent;
    }

    public BlockNode getParent(){
        return parent;
    }

    public VarInitNode getVariable(String lexeme) {
        return localVariables.get(lexeme);
    }
}
