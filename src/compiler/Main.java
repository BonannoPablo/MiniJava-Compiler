package compiler;

import compiler.exceptions.*;
import compiler.lexicalanalyzer.ILexicalAnalyzer;
import compiler.lexicalanalyzer.LexicalAnalyzerWithCases;
import compiler.token.IToken;
import sourcemanager.EfficientSourceManager;
import sourcemanager.SourceManager;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1)
            throw new RuntimeException("Invalid number of arguments. Must specify the source code file path to compile.");

        SourceManager sourceManager = new EfficientSourceManager();
        String filePath = args[0];

        openFile(sourceManager, filePath);
        runLexicalAnalyzer(sourceManager);
        try {
            sourceManager.close();
        } catch (IOException e) {
            System.out.println("There has been an error when reading the source file");
        }
    }

    private static void runLexicalAnalyzer(SourceManager sourceManager) {
        ILexicalAnalyzer lexicalAnalyzer = new LexicalAnalyzerWithCases(sourceManager);
        IToken token = null;
        boolean exceptionFLag = false;
        do {
            try {
                token = lexicalAnalyzer.nextToken();
                printToken(token);
            } catch (LexicalException e) {
                exceptionFLag = true;
                printLexicalException(sourceManager, e);
            }
        } while (token == null || token.getTokenType() != IToken.TokenType.EOF);
        if (!exceptionFLag) {
            System.out.println("[SinErrores]");
        }
    }

    private static void printToken(IToken token) {
        System.out.println("(" + token.getLexeme() + ", " + token.getTokenType() + ", " + token.getLineNumber() + ")");
    }

    private static void printLexicalException(SourceManager sourceManager, LexicalException e) {
        final String RED = "\u001B[31m";
        final String RESET = "\u001B[0m";
        try {
            System.out.println(RED + "\nLexical error in line " + e.getLineNumber() + ", column " + e.getColumnNumber() + ": " + e.getLexeme() + e.getMessage());
            System.out.println("Line " + e.getLineNumber() + ": " + sourceManager.getLine());
            System.out.print(" ".repeat(e.getColumnNumber() + "Line : ".length() + (String.valueOf(e.getLineNumber()).length())-1));
            System.out.println("^");
            System.out.println("[Error:" + e.getLexeme() + "|" + e.getLineNumber() + "]\n" + RESET);
        } catch (IOException ex) {
            System.out.println("There has been an error when reading the source file");
        }
    }

    private static void openFile(SourceManager sm, String filePath) {
        try {
            sm.open(filePath);
        } catch (FileNotFoundException e) {
            System.out.println("File " + filePath + " not found");
            System.exit(1);
        }
    }
}