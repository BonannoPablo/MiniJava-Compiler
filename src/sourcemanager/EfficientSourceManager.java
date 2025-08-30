package sourcemanager;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class EfficientSourceManager implements SourceManager {
    private BufferedReader reader;
    private StringBuilder currentLine;
    private int lineNumber;
    private int lineIndexNumber;
    private boolean mustReadNextLine;


    public EfficientSourceManager() {
        currentLine = new StringBuilder(80);
        lineNumber = 0;
        lineIndexNumber = 0;
        mustReadNextLine = true;
    }

    @Override
    public void open(String filePath) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);

        reader = new BufferedReader(inputStreamReader);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    /*public char getNextChar() throws IOException {
        char currentChar;

        if(mustReadNextLine) {
            currentLine = reader.readLine();
            lineNumber++;
            lineIndexNumber = 0;
            mustReadNextLine = false;
        }

        if(lineIndexNumber < currentLine.length()) {
            currentChar = currentLine.charAt(lineIndexNumber);
            lineIndexNumber++;
        } else if (reader.ready()) {
            currentChar = '\n';
            mustReadNextLine = true;
        } else {
            currentChar = END_OF_FILE;
        }

        return currentChar;
    }*/

    public char getNextChar() throws IOException {
        int currentCharInteger = reader.read();
        if (currentCharInteger == '\r'){
            currentCharInteger = reader.read();
        }
        if (mustReadNextLine) {
            lineNumber++;
            lineIndexNumber = 0;
            mustReadNextLine = false;
            currentLine.delete(0, currentLine.length());
            currentLine.append((char) currentCharInteger);
        } else {
            if(currentCharInteger != '\n')
                currentLine.append((char) currentCharInteger);
            lineIndexNumber++;
        }
        mustReadNextLine = currentCharInteger == '\n';
        return currentCharInteger == -1 ? END_OF_FILE : (char) currentCharInteger;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getLine() throws IOException {
        reader.mark(1);
        String line = mustReadNextLine? currentLine.toString() : currentLine + reader.readLine();
        reader.reset();
        return line;
    }

    public int getLineIndexNumber() {
        return lineIndexNumber;
    }

}