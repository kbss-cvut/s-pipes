package cz.cvut.spipes.audit;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.util.JenaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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