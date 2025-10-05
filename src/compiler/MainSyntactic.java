package compiler;

import compiler.exceptions.*;
import compiler.lexicalanalyzer.LexicalAnalyzer;
import compiler.lexicalanalyzer.LexicalAnalyzerImpl;
import compiler.syntacticanalyzer.SyntacticAnalyzer;
import compiler.syntacticanalyzer.SyntacticAnalyzerImpl;
import compiler.token.Token;
import sourcemanager.EfficientSourceManager;
import sourcemanager.SourceManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Queue;

public class MainSyntactic {
    public static void main2(String[] args) {
        if (args.length != 1)
            throw new RuntimeException("Invalid number of arguments. Must specify the source code file path to compile.");

        String filePath = args[0];
        SourceManager sourceManager = new EfficientSourceManager();
        openFile(sourceManager, filePath);
        SyntacticAnalyzer syntacticAnalyzer = new SyntacticAnalyzerImpl(new LexicalAnalyzerImpl(sourceManager));

        boolean exceptionFlag = false;
        try{
            syntacticAnalyzer.start();
        } catch(LexicalException e){
            printLexicalException(e);
        } catch(SyntacticExceptions e){
            exceptionFlag = true;
            Queue<SyntacticException> exceptionQueue = e.getExceptionsQueue();
            while(!exceptionQueue.isEmpty()) {
                SyntacticException ex = exceptionQueue.poll();
                System.out.println(ex.getMessage());
                System.out.println("[Error:" + ex.getLexeme() + "|" + ex.getLineNumber() + "]");
            }
        }
        if(!exceptionFlag) {
            System.out.println("Compilation successful");
            System.out.println("[SinErrores]");
        }
        try {
            sourceManager.close();
        } catch (IOException e) {
            System.out.println("There has been an error when reading the source file");
        }

    }

    private static void runLexicalAnalyzer(SourceManager sourceManager) {
        LexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzerImpl(sourceManager);
        Token token = null;
        boolean exceptionFLag = false;
        do {
            try {
                token = lexicalAnalyzer.nextToken();
                printToken(token);
            } catch (LexicalException e) {
                exceptionFLag = true;
                printLexicalException(e);
            }
        } while (token == null || token.getTokenType() != Token.TokenType.EOF);
        if (!exceptionFLag) {
            System.out.println("[SinErrores]");
        }


    }

    private static void printToken(Token token) {
        System.out.println("(" + token.getTokenType() + ", " + token.getLexeme() + ", " + token.getLineNumber() + ")");
    }

    private static void printLexicalException(LexicalException e) {
        final String RED = "\u001B[31m";
        final String RESET = "\u001B[0m";
        System.out.println(RED + "\nLexical error in line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ": " + e.getLexeme() + e.getMessage());
        System.out.println("Line " + e.getLineNumber() + ": " + e.getLine());
        System.out.print(" ".repeat(e.getColumnNumber() + "Line : ".length() + (String.valueOf(e.getLineNumber()).length())-1));
        System.out.println("^");
        System.out.println("[Error:" + e.getLexeme() + "|" + e.getLineNumber() + "]\n" + RESET);
    }

    private static void openFile(SourceManager sm, String filePath) {
        try {
            sm.open(filePath);
        } catch (FileNotFoundException e) {
            System.out.println("File " + filePath + " not found");
            System.exit(1);
        }
        int x,y;
        int z = 1;

    }
}