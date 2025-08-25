package compiler;

import compiler.lexicalanalyzer.ILexicalAnalyzer;
import compiler.lexicalanalyzer.LexicalAnalyzer;
import sourcemanager.SourceManager;
import sourcemanager.SourceManagerImpl;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String args[]) {
        SourceManager sm = new SourceManagerImpl();
        try {
            sm.open("src/compiler/test.txt");
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        }
        ILexicalAnalyzer la = new LexicalAnalyzer(sm);

        for(int i = 0; i < 20; i++){
            System.out.println(la.nextToken().getTokenValue());
        }
        try {
            sm.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
