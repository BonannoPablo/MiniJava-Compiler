package compiler.ast;

import java.util.LinkedList;
import java.util.List;

public class BlockNode extends SentenceNode{
    protected List<SentenceNode> sentences;

    public BlockNode(){
        sentences = new LinkedList<>();
    }

    public void addSentence(SentenceNode sentence){
        sentences.addLast(sentence);
    }

    public void print(int level){
        System.out.println(" ".repeat(level)+"BLOCK");
        for(var sentence : sentences)
            sentence.print(level+1);
    }

}
