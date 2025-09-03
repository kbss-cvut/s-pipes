package cz.cvut.jena;


import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntDocumentManagerTest {

    private static final Logger log = LoggerFactory.getLogger(OntDocumentManagerTest.class);

    @Test
    public void getOntologyTriggersReadFailureHandler() {
        OntDocumentManager docManager = OntDocumentManager.getInstance();
        final boolean[] readFailureHandlerIsTriggered = {false};
        OntDocumentManager.ReadFailureHandler handler = (url, model, e) -> {
            log.info("- url: {}", url);
            log.info("- model: {}", model);
            readFailureHandlerIsTriggered[0] = true;
        };
        docManager.setReadFailureHandler(handler);
        Assertions.assertFalse(readFailureHandlerIsTriggered[0]);
        OntModel m = docManager.getOntology("http://example.org/not-exists", OntModelSpec.OWL_MEM);
        Assertions.assertTrue(readFailureHandlerIsTriggered[0]);
    }
}

