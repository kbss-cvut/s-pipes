package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.manager.OntoDocManager;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import org.apache.jena.assembler.JA;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module returns prefix mappings of all loaded scripts.
 * Individual mappings are represented by following graph pattern:
 *
 * @prefix ja: <http://jena.hpl.hp.com/2005/11/Assembler#> .
 *
 * ?ontology ja:prefixMapping 
 *      [ a ja:SinglePrefixMapping ;
 *          ja:namespace ?namespaceIRI ;
 *          ja:prefix ?prefix ]
 * .
 *
 * As an example let's assume we loaded only one script:
 *
 * @prefix : <http://example.org/> .
 * @prefix owl: <http://www.w3.org/2002/07/owl#> .
 *
 * :my-ontology a owl:Ontology .
 *
 *
 * The output of this module for the example script would be:
 *
 * @prefix : <http://example.org/> .
 * @prefix ja: <http://jena.hpl.hp.com/2005/11/Assembler#> .
 *
 * :my-ontology ja:prefixMapping 
 *      [ a ja:SinglePrefixMapping ;
 *          ja:namespace "http://example.org/" ;
 *          ja:prefix "" ],
 *      [ a ja:SinglePrefixMapping ;
 *          ja:namespace "http://www.w3.org/2002/07/owl#" ;
 *          ja:prefix "owl" ] 
* .
 */
public class RetrievePrefixesModule extends AnnotatedAbstractModule {

    private static final Logger log = LoggerFactory.getLogger(RetrievePrefixesModule.class);
    private static final String TYPE_URI = KBSS_MODULE.uri + "retrieve-prefixes";

    //sml:replace
    @Parameter(urlPrefix = SML.uri, name = "replace", comment = "")
    private boolean isReplace = false;

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

        return this.createOutputContext(isReplace, outputModel);
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

//    @Override
//    public void loadConfiguration() {
//        isReplace = this.getPropertyValue(SML.replace, false);
//    }

    void setOntologyDocumentManager(OntologyDocumentManager ontologyDocumentManager) {
        this.ontologyDocumentManager = ontologyDocumentManager;
    }
}