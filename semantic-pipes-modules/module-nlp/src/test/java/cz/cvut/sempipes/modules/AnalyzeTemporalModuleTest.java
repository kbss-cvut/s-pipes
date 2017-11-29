package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class AnalyzeTemporalModuleTest {


    @Ignore
    @Test
    public void executeSelf() throws Exception {


        AnalyzeTemporalModule temporalModule = new AnalyzeTemporalModule();
        ExecutionContext inCtx = ExecutionContextFactory.createEmptyContext();
        temporalModule.setInputContext(inCtx);

        temporalModule.setSparqlEndpoint("http://linked.opendata.cz/sparql/");
        temporalModule.setDatasetNamedGraph("<http://linked.opendata.cz/resource/dataset/vavai/evaluation/2009>");
        //TODO move the query to external file
//        temporalModule.setq("CONSTRUCT {?s ?p ?o}\n" +
//                "From Named \n" +
//                temporalModule.datasetNamedGraph +
//                "WHERE {\n" +
//                " GRAPH ?g {\n" +
//                " \n" +
//                "        ?s ?p ?o .\n" +
//                //"        FILTER(regex(?o, \"more than\")).\n" +
//                "        FILTER(isLiteral(?o) && regex(str(?o), \"[0-9]\", \"i\"))" +
//                "       \n" +
//                " }\n" +
//                "       \n" +
//                "}" +
//                "LIMIT 200"
//        );

                temporalModule.setq("CONSTRUCT {?s ?p ?o}\n" +
                "From Named \n" +
                temporalModule.datasetNamedGraph +
                "WHERE {\n" +
                " GRAPH ?g {\n" +
                " \n" +
                "        ?s ?p ?o .\n" +
                //"        FILTER(regex(?o, \"more than\")).\n" +
                "        FILTER(isLiteral(?o) && regex(str(?o), \"[0-9]\", \"i\") &&" +
               // "        ?p = <http://purl.org/dc/terms/title> || \n" +
//                "        ?p = <http://purl.org/dc/terms/subject> || \n" +
                "          ?p = <http://purl.org/dc/terms/title> \n" +
                "       \n" +
                " )\n" +
                "   }    \n" +
                "} LIMIT 7000"
        );


        ExecutionContext outctx = temporalModule.executeSelf();
        Model outModel = outctx.getDefaultModel();

        System.out.println("-------------------------------------");
        System.out.println(outModel.size());

        //outModel.write(System.out);


        //query output model
        String q1 = "prefix analyze: <http://onto.fel.cvut.cz/ontologies/lib/module/analyze/>\n" +
                "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "\n" +
                "SELECT ?temporalExpression ?Begin ?End  ?DateType ?sourceText \n" +
                "WHERE {\n" +
                "    ?ext a analyze:sutime-extraction .\n" +
                "    ?ext rdf:object ?sourceText .\n" +
                "    ?ext analyze:extracted ?temporalExpression .     \n" +
                "    ?ext analyze:beginDate ?Begin .\n" +
                "    ?ext analyze:endDate ?End .\n" +
                "    ?ext analyze:type ?DateType .\n" +
                "}" +
                "ORDER BY (?Begin) (?End)";

        //query output model to extract min and max dates
        //String q2 = "";


        File resultsFile = new File("src/test/resources/results.json");
        QueryExecution qexec = QueryExecutionFactory.create(q1, outModel);
        ResultSet results = qexec.execSelect();
        writeResultToFile(results, resultsFile);


    }

    public void writeResultToFile(ResultSet rs, File file){
        //working output as JSON
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, rs);
        String json = new String(outputStream.toByteArray());
        //System.out.println(json);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(json);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void testAccuracy(){
        String ep = "http://linked.opendata.cz/sparql/";
        String ng = "<http://linked.opendata.cz/resource/dataset/legislation/psp.cz>";
        String qu = "SELECT {?s ?v ?t}" +
        "From Named \n" +
                ng +
                "WHERE {\n" +
                " GRAPH ?g {\n" +
                " \n" +
                "?s <http://purl.org/dc/terms/valid> ?v ." +
                "?s <http://purl.org/dc/terms/title> ?t" +
                "        FILTER(isLiteral(?t) && regex(str(?o), \"[0-9]\", \"i\")" +
                // "        ?p = <http://purl.org/dc/terms/title> || \n" +
//                "        ?p = <http://purl.org/dc/terms/subject> || \n" +
               // "          ?p = <http://purl.org/dc/terms/title> \n" +
                "       \n" +
                " )\n" +
                "   }    \n" +
                "} LIMIT 200";
        ResultSet rs = QueryExecutionFactory.sparqlService(ep, qu).execSelect();




    }
}
