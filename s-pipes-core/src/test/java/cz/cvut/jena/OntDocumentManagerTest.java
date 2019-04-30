package cz.cvut.jena;


import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntDocumentManagerTest {

    private static final Logger LOG = LoggerFactory.getLogger(OntDocumentManagerTest.class);

    @Test
    public void getOntologyTriggersReadFailureHandler() {
        OntDocumentManager docManager = OntDocumentManager.getInstance();
        final boolean[] readFailureHandlerIsTriggered = {false};
        OntDocumentManager.ReadFailureHandler handler = new OntDocumentManager.ReadFailureHandler() {
            @Override
            public void handleFailedRead(String url, Model model, Exception e) {
                LOG.info("- url: " + url);
                LOG.info("- model: " + model);
                readFailureHandlerIsTriggered[0] = true;
                return;
            }
        };
        docManager.setReadFailureHandler(handler);
        Assertions.assertFalse(readFailureHandlerIsTriggered[0]);
        OntModel m = docManager.getOntology("http://example.org/not-exists", OntModelSpec.OWL_MEM);
        Assertions.assertTrue(readFailureHandlerIsTriggered[0]);
    }
}

