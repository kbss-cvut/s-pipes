package cz.cvut.spipes.logging;

import cz.cvut.spipes.util.TempFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FileSystemLogger {

    private static Path root;

    private static final Logger log =
            LoggerFactory.getLogger(FileSystemLogger.class);

    static {
        try {
            root = Files.createTempDirectory(TempFileUtils.createTimestampFileName("-s-pipes-log-"));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    static Path resolvePipelineExecution(long pipelineExecutionId) {
        return root.resolve("pipeline-execution-" + pipelineExecutionId);
    }

    static String getModuleOutputFilename(final String moduleExecutionId) {
        return "module-" + moduleExecutionId + "-output.ttl";
    }

    static String getModuleInputFilename(final String moduleExecutionId) {
        return "module-" + moduleExecutionId + "-input.ttl";
    }
}
