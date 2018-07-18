package cz.cvut.spipes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelLogger {

    private static final Logger LOG = LoggerFactory.getLogger(ModelLogger.class);
    private Logger classLogger;
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
                model.write(Files.newOutputStream(filePath), FileUtils.langTurtle);
            } catch (IOException e) {
                LOG.warn("Unnable to save model to " + filePath);
            }
        }
    }

    private Path getLogDirectory() {
        try {
            if (logDirectory == null) {
                logDirectory = Files.createTempDirectory(logDirectoryPrefix);
            }
        } catch (IOException e) {
            LOG.warn("Unnable to create temporary directory with prefix " + logDirectoryPrefix);
        }
        return logDirectory;
    }


}
