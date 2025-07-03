package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class SUTimeModuleIntegrationTest {

    /********************
     * Use for manual tests
     ********************/
    @Disabled
    @Test
    public void deployManual() {
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/vavai/programmes");
    }

    @Disabled
    @Test
    public void deployAll() {
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/vavai/programmes");
        deployTemporalExtractionLinkedDataCz("http://ruian.linked.opendata.cz/resource/dataset/cuzk/pravni-vztahy-listiny");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/political-parties-cz");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/seznam.gov.cz/rejstriky/plneni");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/vavai/tenders");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/seznam.gov.cz/agendy");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/seznam.gov.cz/rejstriky/objednavky");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/vavai/research-plans");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/seznam.gov.cz/rejstriky/smlouvy");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/legislation/psp.cz");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/coi.cz/sankce");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/currency");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/drugbank");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/vavai/evaluation/2009");
        deployTemporalExtractionLinkedDataCz("http://linked.opendata.cz/resource/dataset/coi.cz/kontroly");
    }

    private Model getModel(String endpointUrl, String namedGraphUri) {
        String query = "CONSTRUCT {?s ?p ?o } FROM { GRAPH <" + namedGraphUri + "> { ?s ?p ?o } }";

        return QueryExecutionHTTP.service(endpointUrl, query).execConstruct();
    }

    private void deployTemporalExtraction(String inputEndpointUrl, String namedGraphUri, String outputSesameServerUrl, String outputRepositoryName) {

        Model inputModel = getModel(inputEndpointUrl, namedGraphUri);

        SUTimeModule module = new SUTimeModule();

        module.setInputContext(ExecutionContextFactory.createContext(inputModel));

        Model outputModel = module.executeSelf().getDefaultModel();

        deployModel(outputModel, outputSesameServerUrl, outputRepositoryName, namedGraphUri);

    }

    private void deployModel(Model model, String outputSesameServerUrl, String outputRepositoryName, String namedGraphUri) {
        Rdf4jDeployModule m = new Rdf4jDeployModule();
        m.setInputContext(ExecutionContextFactory.createContext(model));
        m.setRdf4jServerURL(outputSesameServerUrl);
        m.setRdf4jRepositoryName(outputRepositoryName);
        m.setRdf4jContextIRI(namedGraphUri);
        m.setReplaceContext(true);
        m.executeSelf();
    }

    private void deployTemporalExtractionLinkedDataCz(String namedGraphUri) {
        deployTemporalExtraction("http://linked.opendata.cz/sparql", namedGraphUri, "http://onto.fel.cvut.cz/rdf4j-server", "test");

    }


}