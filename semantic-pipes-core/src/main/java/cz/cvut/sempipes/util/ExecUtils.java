package cz.cvut.sempipes.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Miroslav Blasko on 26.5.16.
 */
public class ExecUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ExecUtils.class);

    public static File stream2file(InputStream in) throws IOException {
        File tempFile = Files.createTempFile("execution-", ".txt").toFile();

        //tempFile.deleteOnExit();
        LOG.trace("Using temporary file for input stream " + tempFile.getAbsolutePath());
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }

    public static File createTempFile() {
        File tempFile = null;
        try {
            tempFile = Files.createTempFile("execution-", ".txt").toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOG.trace("Using temporary file for output stream " + tempFile.getAbsolutePath());
        return tempFile;
    }

    public static InputStream execProgram(String[] programCall, InputStream inputStream) throws IOException, InterruptedException {

        // set program call
        ProcessBuilder procBuilder = new ProcessBuilder();
        procBuilder.command(programCall);

        // set input
        if (inputStream != null) {
            procBuilder.redirectInput(ExecUtils.stream2file(inputStream));
        }

        File tempOutputFile = ExecUtils.createTempFile();
        procBuilder.redirectOutput(tempOutputFile);

        // wait for execution to finish
        Process proc = procBuilder.start();
        int returnVal = proc.waitFor();

        // get output
        InputStream tempIS = new FileInputStream(tempOutputFile);
        //OutputStream os = new org.apache.commons.io.output.ByteArrayOutputStream();

        //IOUtils.copy(tempIS, System.out);
        //IOUtils.copy(tempIS, os);

        return tempIS;
    }

    //TODO remove
    public static InputStream execProgramWithoutExeption(String[] programCall, InputStream inputStream) {
        String programCallStr = "\"" + Arrays.asList(programCall).stream().collect(Collectors.joining(" ")) + "\"" ;
        LOG.debug("Executing -- " + programCallStr);
        try {
            return execProgram(programCall, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
