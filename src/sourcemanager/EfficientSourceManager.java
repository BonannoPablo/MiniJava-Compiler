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
            if(currentCharInteger != '\n' && currentCharInteger != -1)
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
        reader.mark(8192 );
        String reminderOfTheLine = reader.readLine();
        String line = mustReadNextLine? currentLine.toString() : currentLine + (reminderOfTheLine == null ? "" : reminderOfTheLine);
        reader.reset();
        return line;
    }

    public int getLineIndexNumber() {
        return lineIndexNumber;
    }

}