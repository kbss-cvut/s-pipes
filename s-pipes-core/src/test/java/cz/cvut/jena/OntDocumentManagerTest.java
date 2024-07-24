package cz.cvut.jena;


import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class OntDocumentManagerTest {

    @Test
    public void getOntologyTriggersReadFailureHandler() {
        OntDocumentManager docManager = OntDocumentManager.getInstance();
        final boolean[] readFailureHandlerIsTriggered = {false};
        OntDocumentManager.ReadFailureHandler handler = new OntDocumentManager.ReadFailureHandler() {
            @Override
            public void handleFailedRead(String url, Model model, Exception e) {
                log.info("- url: " + url);
                log.info("- model: " + model);
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

