package cz.cvut.sempipes.audit;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.manager.OntoDocManager;
import cz.cvut.sempipes.util.JenaUtils;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Miroslav Blasko on 14.1.17.
 */
public class Audit {

    private static Logger LOG = LoggerFactory.getLogger(Audit.class);

    public void recordExecutionContext(ExecutionContext executionContext) {

        // record bindings
        // record model

        JenaUtils.saveModelToTemporaryFile(executionContext.getDefaultModel());
    }

}
