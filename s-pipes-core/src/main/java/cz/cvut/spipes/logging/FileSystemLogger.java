package cz.cvut.spipes.logging;

import cz.cvut.spipes.util.TempFileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class FileSystemLogger {

    private static Path root;

    static {
        try {
            root = Files.createTempDirectory(TempFileUtils.createTimestampFileName("-s-pipes-log-"));
        } catch (IOException e) {
            e.printStackTrace();
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
