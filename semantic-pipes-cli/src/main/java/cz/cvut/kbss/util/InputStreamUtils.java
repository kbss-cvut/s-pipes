/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.kbss.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author blcha
 */
public class InputStreamUtils {

    private static Logger LOG = LoggerFactory.getLogger(InputStreamUtils.class);

    /**
     * Returns list of rows, where each row is list of words.
     * It is read form input stream <code>is</code> and words are separated
     * by specified delimiter.
     *
     * @param is
     * @param delimiter
     * @return
     * @throws IOException
     */
    public static List<List<String>> getWordRows(InputStream is, String delimiter) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        List<List<String>> rowList = new LinkedList<List<String>>();
        String line;


        //if (br.ready())


        while ((line = br.readLine()) != null) {

            String[] wordsOnLine = line.split(delimiter);
            List<String> row = Arrays.asList(wordsOnLine);
            rowList.add(row);
        }

        return rowList;
    }

    public static InputStream teeToTemporaryFile(InputStream is) {


        File tempFile = null;
        try {

            tempFile = File.createTempFile("generated-ontology", ".owl");
            LOG.info("Saving ontology to " + tempFile.getAbsolutePath());
            OutputStream os = new FileOutputStream(tempFile);
            IOUtils.copy(is, os);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);

            return new FileInputStream(tempFile);
        } catch (IOException ex) {
            LOG.error("Could not copy file from input-stream " + is + " :" + ex);
        }

        return null;
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        is.close();
        return sb.toString();
    }
}
