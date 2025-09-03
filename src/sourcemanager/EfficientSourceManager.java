package sourcemanager;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class EfficientSourceManager implements SourceManager {
    private BufferedReader reader;
    private StringBuilder currentLine;
    private int lineNumber;
    private int lineIndexNumber;
    private boolean mustReadNextLine;
    private boolean readReminderOfTheLine;
    private String reminderOfTheLine;
    private int reminderOfTheLineOffset;


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
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String getLine() throws IOException {
        reminderOfTheLine = reader.readLine();
        reminderOfTheLineOffset = lineIndexNumber;
        String line = mustReadNextLine ? currentLine.toString() : currentLine + (reminderOfTheLine == null ? "" : reminderOfTheLine);
        readReminderOfTheLine = reminderOfTheLine != null && !mustReadNextLine;
        return line;
    }

    @Override
    public int getLineIndexNumber() {
        return lineIndexNumber;
    }

    @Override
    public char getNextChar() throws IOException {
        int currentCharInteger = readNextCharacter();

        if (mustReadNextLine) {
            lineNumber++;
            lineIndexNumber = 0;
            mustReadNextLine = false;
            currentLine.delete(0, currentLine.length());
            currentLine.append((char) currentCharInteger);
        } else {
            if (currentCharInteger != '\n' && currentCharInteger != -1)
                currentLine.append((char) currentCharInteger);
            lineIndexNumber++;
        }
        mustReadNextLine = currentCharInteger == '\n';
        return currentCharInteger == -1 ? END_OF_FILE : (char) currentCharInteger;
    }

    private int readNextCharacter() throws IOException {
        if (!readReminderOfTheLine) {
            int nextChar = reader.read();
            if (nextChar == '\r') {
                nextChar = reader.read();
            }
            return nextChar;
        } else if (lineIndexNumber - reminderOfTheLineOffset < reminderOfTheLine.length()) {
            return reminderOfTheLine.charAt(lineIndexNumber - reminderOfTheLineOffset);
        } else {
            readReminderOfTheLine = false;
            return '\n';
        }
    }
}