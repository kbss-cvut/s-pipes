package cz.cvut.spipes.audit;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.util.JenaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Audit {

    private static final Logger log = LoggerFactory.getLogger(Audit.class);

    public void recordExecutionContext(ExecutionContext executionContext) {

        // record bindings
        // record model

        JenaUtils.saveModelToTemporaryFile(executionContext.getDefaultModel());
    }

}
