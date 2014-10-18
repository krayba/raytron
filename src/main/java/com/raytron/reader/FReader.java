/**
 * 
 */
package com.raytron.reader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

/**
 * @author kedar460043
 * 
 */
public class FReader {

    private final BufferedReader breader;
    private final long totalLength;
    private long lengthRead;
    private String line;
    private boolean lineExist;
    private final List<String> header = new ArrayList<String>(30);
    private final boolean includeHeader;
    private final int noOfLines;
    private String maxLine;

    /**
     * @throws FileNotFoundException
     * 
     */
    public FReader(final Path path, final boolean includeHeader) throws IOException {
        File file = path.toFile();
        totalLength = file.length();
        noOfLines = countLines(file);
        lengthRead = 0;
        breader = new BufferedReader(new FileReader(file));
        this.includeHeader = includeHeader;
        populateHeader();
    }

    /**
     * @throws IOException
     * 
     */
    private void populateHeader() throws IOException {
        if (hasNext()) {
            header.addAll(tokenizeLine());
        }
    }

    private List<String> tokenizeLine() {
        StrTokenizer tokenizer = new StrTokenizer(line, StrMatcher.commaMatcher(), StrMatcher.doubleQuoteMatcher());
        List<String> list = new ArrayList<String>();
        while (tokenizer.hasNext()) {
            list.add((String) tokenizer.next());
        }
        return list;
    }

    public boolean hasNext() throws IOException {
        line = breader.readLine();
        lineExist = (line != null);
        if (lineExist) {
            lengthRead += line.length();
        } else {
            lengthRead = totalLength;
        }
        return lineExist;
    }

    public String next() throws IOException {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String str : tokenizeLine()) {
            if (!(str == null || "".equals(str))) {
                if (index < header.size() && includeHeader) {
                    sb.append(header.get(index++));
                    sb.append(":\n");
                    sb.append("\t");
                }
                sb.append(str);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * @return
     */
    public int getPercentageRead() {
        return (int) ((lengthRead * 100) / totalLength);
    }

    /**
     * @throws IOException
     */
    public void close() throws IOException {
        breader.close();
    }

    private int countLines(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            int maxLength = 0;
            int lineLength = 0;
            StringBuilder sb = new StringBuilder(1024);
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    lineLength++;
                    sb.append((char) c[i]);
                    if (c[i] == '\n') {
                        if (lineLength > maxLength) {
                            maxLength = lineLength;
                            maxLine = sb.toString();
                        }
                        lineLength = 0;
                        sb.delete(0, sb.length());
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }

    /**
     * @return the noOfLines
     */
    public int getNoOfLines() {
        return noOfLines;
    }

    public int getMaxLength() throws IOException {
        return getMaxLine().length();
    }

    public List<String> getHeader() {
        return header;
    }

    /**
     * @return the maxLine
     * @throws Throwable
     */
    public String getMaxLine() throws IOException {
        String orgLine = line;
        line = maxLine;
        String newLine = next();
        line = orgLine;
        return newLine;
    }

}
