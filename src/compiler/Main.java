package compiler;

import compiler.exceptions.*;
import compiler.lexicalanalyzer.ILexicalAnalyzer;
import compiler.lexicalanalyzer.LexicalAnalyzerWithCases;
import compiler.token.IToken;
import sourcemanager.SourceManager;
import sourcemanager.SourceManagerImpl;

import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        SourceManager sm = new SourceManagerImpl();
        String filePath = "";
        if (args.length == 1) {
            filePath = args[0];
        } else
            throw new RuntimeException("Invalid number of arguments. Must specify the source code file path to compile.");
        try {
            sm.open(filePath);
        } catch (FileNotFoundException e) {
            System.out.println("File " + filePath + " not found");
        }

        ILexicalAnalyzer la = new LexicalAnalyzerWithCases(sm);
        IToken token = null;
        boolean exceptionFLag = false;
        do {
            try {
                token = la.nextToken();
                System.out.println("(" + token.getLexeme() + ", " + token.getTokenType() + ", " + token.getLineNumber() + ")");
            } catch (LexicalException e) {
                exceptionFLag = true;
                System.out.println("\nLexical error in line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ": "+ e.getLexeme() + e.getMessage());
                System.out.println("Line " + e.getLineNumber() + ": " + sm.getLine());
                for(int i = 0; i < e.getColumnNumber() + "Line 1:".length()-1; i++){
                    System.out.print(" ");
                }
                System.out.println("^");
                System.out.println("[Error:" + e.getLexeme() + "|" + e.getLineNumber() + "]\n");
                System.out.println(e.getColumnNumber());
            }
        } while (token == null || token.getTokenType() != IToken.TokenType.EOF);
        if(!exceptionFLag){
            System.out.println("[SinErrores]");
        }
    }
}