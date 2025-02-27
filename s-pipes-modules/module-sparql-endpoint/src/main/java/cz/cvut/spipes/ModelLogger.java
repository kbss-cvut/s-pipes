package cz.cvut.spipes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import cz.cvut.spipes.util.JenaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;

@Slf4j
public class ModelLogger {

    private final Logger classLogger;
    private String logDirectoryPrefix = "";
    private Path logDirectory;

    public ModelLogger(String logDirectoryPrefix, Logger classLogger) {
        this.logDirectoryPrefix = logDirectoryPrefix;
        this.classLogger = classLogger;
    }

    public void trace(String modelId, Model model) {
        if (classLogger.isTraceEnabled()) {
            Path filePath = null;
            try {
                filePath = getLogDirectory().resolve(modelId + ".ttl");
                classLogger.trace("Saving model with id " + modelId + " to " + filePath);
                JenaUtils.write(model, Files.newOutputStream(filePath));
            } catch (IOException e) {
                log.warn("Unnable to save model to " + filePath);
            }
        }
    }

    private Path getLogDirectory() {
        try {
            if (logDirectory == null) {
                logDirectory = Files.createTempDirectory(logDirectoryPrefix);
            }
        } catch (IOException e) {
            log.warn("Unnable to create temporary directory with prefix " + logDirectoryPrefix);
        }
        return logDirectory;
    }


}
