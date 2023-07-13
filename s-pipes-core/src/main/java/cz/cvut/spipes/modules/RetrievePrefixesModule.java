package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.manager.OntoDocManager;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import org.apache.jena.assembler.JA;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetrievePrefixesModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(RetrievePrefixesModule.class.getName());

    private static String TYPE_URI = KBSS_MODULE.getURI() + "retrieve-prefixes";

    //TODO refactor -> should be part of execution context
    OntologyDocumentManager ontologyDocumentManager = OntoDocManager.getInstance();

    @Override
    ExecutionContext executeSelf() {
        Model outputModel = ModelFactory.createDefaultModel();

        for (String ontologyUri : ontologyDocumentManager.getRegisteredOntologyUris()) {

            Resource ontology = outputModel.createResource(ontologyUri);

            ontologyDocumentManager.getOntology(ontologyUri).getNsPrefixMap().forEach((key, value) -> {
                Resource singlePrefixMapping = outputModel.createResource();

                outputModel.add(ontology, JA.prefixMapping, singlePrefixMapping);

                outputModel.add(
                    singlePrefixMapping, RDF.type, JA.SinglePrefixMapping
                );
                outputModel.add(
                    singlePrefixMapping, JA.prefix, key
                );
                outputModel.add(
                    singlePrefixMapping, JA.namespace, value
                );
            });
        }

        return ExecutionContextFactory.createContext(outputModel);

    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {
    }

    void setOntologyDocumentManager(OntologyDocumentManager ontologyDocumentManager) {
        this.ontologyDocumentManager = ontologyDocumentManager;
    }
}