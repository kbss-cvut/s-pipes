package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.util.QueryUtils;
import java.io.IOException;
import java.util.Date;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

public class ImproveSPOWithMarginalsModuleTest {
    @Ignore
    @Test
    public void executeSelf() throws Exception {

        System.out.println("Begin test time start : " + new Date());

        ImproveSPOWithMarginalsModule module = new ImproveSPOWithMarginalsModule();

        module.setDataServiceUrl("https://linked.opendata.cz/sparql?default-graph-uri=http%3A%2F%2Flinked.opendata.cz%2Fresource%2Fdataset%2Fvavai%2Fprogrammes");
//        module.setMarginalsDefsFileUrl("file:///home/blcha/projects/gacr/task/2017-09-05-compute-spo-with-marginals/wdr/cz-all.wdr-definitions.ttl");
//        module.setMarginalsDefsFileUrl("file:///home/blcha/projects/gacr/task/2017-09-05-compute-spo-with-marginals/wdr/cz-all.wdr-definitions-reduced.ttl");
        module.setMarginalsDefsFileUrl("file:///home/blcha/projects/gacr/task/2017-09-05-compute-spo-with-marginals/wdr/cz-all.wdr-definitions-reduced-vavai-programes.ttl");


        module.setMarginalConstraint("#\t\t\tno type is marginal\n" +
                "            FILTER NOT EXISTS {\n" +
                "\t\t\t      ?s a ?c .\n" +
                "     \t\t      FILTER (?c != owl:NamedIndividual && ?c!=owl:Thing && ?c!=rdfs:Resource)\n" +
                "            }");

        module.setInputContext(
            ExecutionContextFactory.createContext(
                loadModelFromResourcePath("/input-spo-summary.ttl"),
                loadVariableBindings("/input-binding.ttl")));

        ExecutionContext outputEC = module.executeSelf();

        assertEquals(outputEC.getDefaultModel().size(), 10);
    }


    private VariablesBinding loadVariableBindings(String resourcePath) {
        VariablesBinding variablesBinding = new VariablesBinding();
        try {
            variablesBinding.load(getClass().getResourceAsStream(resourcePath), FileUtils.langTurtle);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load variable binding from resource path " + resourcePath + ": ", e);
        }
        return variablesBinding;
    }

    private Model loadModelFromResourcePath(String resourcePath) {
        return ModelFactory.createDefaultModel().read(getClass().getResourceAsStream(resourcePath), "", FileUtils.langTurtle);
    }

//    @Test
//    public void loadDataset() {
//        System.out.println("- loading time start : " + new Date());
//        Model marginalDefsModel = loadMarginalDefs();
//        System.out.println("- loading time end: " + new Date());
//        System.out.println("- triples count: "  + marginalDefsModel.size());
//        System.out.println("- querying time start: " + new Date());
//        Model queryResultModel  = executeConstructQuery(marginalDefsModel);
//        JenaUtils.saveModelToTemporaryFile(queryResultModel);
//        System.out.println("- querying time end: " + new Date());
//        System.out.println("- triples count: "  + queryResultModel.size());
//    }

//    private Model loadMarginalDefs() {
//        Model marginalDefsModel = ModelFactory.createDefaultModel();
//        marginalDefsModel.read("file:///home/blcha/projects/gacr/task/2017-09-05-compute-spo-with-marginals/wdr/cz-all.wdr-definitions-reduced.ttl");
//        return marginalDefsModel;
//    }

    private Model executeConstructQuery(Model model) {
        String queryStr = "PREFIX :<http://onto.fel.cvut.cz/ontologies/dataset-descriptor/s-p-o-summary/>\n" +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX ld-cube: <http://purl.org/linked-data/cube#>\n" +
            "PREFIX ddo: <http://onto.fel.cvut.cz/ontologies/dataset-descriptor/>\n" +
            "\n" +
            "CONSTRUCT {\n" +
            "      ?observation ddo:weakly-described-resource ?resource ;\n" +
            "                   ddo:type ?resourceType .\n" +
            "}\n" +
            "WHERE {\n" +
            "      ?observation ddo:weakly-described-resource ?resource ;\n" +
            "                   ddo:type ?resourceType .\n" +
            "}";

        Query query = QueryFactory.create();
        QueryFactory.parse(
                query,
                queryStr,
                "",
                Syntax.syntaxSPARQL_11);
        return QueryUtils.execConstruct(query, model, null);
    }


}