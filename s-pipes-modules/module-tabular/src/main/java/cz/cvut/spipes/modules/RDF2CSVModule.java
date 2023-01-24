package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.*;

public class RDF2CSVModule extends AbstractModule {

    public static final String TYPE_URI = KBSS_MODULE.uri + "RDF2CSV";
    private static final Logger LOG = LoggerFactory.getLogger(RDF2CSVModule.class);

    private final Property P_DATE_PREFIX = getSpecificParameter("data-prefix");
    private final Property P_FILE_OUTPUT_PATH = getSpecificParameter("file-output-path");

    //:data-prefix
    private String dataPrefix;

    //:file-output-path
    private String fileOutputPath;

    @Override
    ExecutionContext executeSelf(){
        Model inputRDF = this.getExecutionContext().getDefaultModel();

        try(BufferedWriter simpleWriter = new BufferedWriter(new FileWriter(fileOutputPath, false))){

            writeStringsIntoRow(simpleWriter, "DocumentId","DocumentLineNumber", "WorkOrderId","TaskCardId",
                    "ComponentURI","ComponentLabel","ComponentScore","MultipleComponents");
            writeStringsIntoRow(simpleWriter
                    ,"FailureURI","FailureLabel","FailureScore","MultipleFailures",
                    "AggregateScore","IsConfirmed" ,"OriginalText","AnnotatedText");
            simpleWriter.append("\n");

            List<Resource> rows = inputRDF
                    .listStatements()
                    .filterKeep(st -> st.getObject().toString().equals(dataPrefix + "Row"))
                    .mapWith(Statement::getSubject).toList();

            rows.sort((o1, o2) -> {
                int i1 = parseInt(o1
                        .getProperty(inputRDF.getProperty(dataPrefix + "DocumentLineNumber"))
                        .getObject()
                        .toString());
                int i2 = parseInt(o2
                        .getProperty(inputRDF.getProperty(dataPrefix + "DocumentLineNumber"))
                        .getObject()
                        .toString());
                return Integer.compare(i1, i2);
            }) ;

            for (Resource res : rows) {
                Statement docId = res.getProperty(inputRDF.getProperty(dataPrefix + "TODO"));
                Statement lineNumber = res.getProperty(inputRDF.getProperty(dataPrefix + "DocumentLineNumber"));
                Statement woID = res.getProperty(inputRDF.getProperty(dataPrefix + "WorkOrderId"));
                Statement tcID = res.getProperty(inputRDF.getProperty(dataPrefix + "TaskCardId"));
                Statement compUri = res.getProperty(inputRDF.getProperty(dataPrefix + "ComponentUri"));
                Statement compLabel = res.getProperty(inputRDF.getProperty(dataPrefix + "ComponentLabel"));
                Statement compScore = res.getProperty(inputRDF.getProperty(dataPrefix + "ComponentScore"));
                Statement failureUri = res.getProperty(inputRDF.getProperty(dataPrefix + "FailureUri"));
                Statement failureLabel = res.getProperty(inputRDF.getProperty(dataPrefix + "FailureLabel"));
                Statement failureScore = res.getProperty(inputRDF.getProperty(dataPrefix + "FailureScore"));
                Statement aggregateScore = res.getProperty(inputRDF.getProperty(dataPrefix + "AggregateScore"));
                Statement isConfirmed = res.getProperty(inputRDF.getProperty(dataPrefix + "IsConfirmed"));
                Statement originalText = res.getProperty(inputRDF.getProperty(dataPrefix + "OriginalText"));
                Statement annotatedText = res.getProperty(inputRDF.getProperty(dataPrefix + "AnnotatedText"));

                StmtIterator multipleComps = res.listProperties(inputRDF.getProperty(dataPrefix + "MultipleComponents"));
                StmtIterator multipleFailures = res.listProperties(inputRDF.getProperty(dataPrefix + "MultipleFailures"));


                writeStringsIntoRow(simpleWriter,
                        getStringValue(docId),
                        getStringValue(lineNumber),
                        getStringValue(woID),
                        getStringValue(tcID),
                        getStringValue(compUri),
                        getLiteralValue(compLabel),
                        getLiteralValue(compScore),
                        getMultipleObjectValues(multipleComps));

                writeStringsIntoRow(simpleWriter,
                        getStringValue(failureUri),
                        getLiteralValue(failureLabel),
                        getLiteralValue(failureScore),
                        getMultipleObjectValues(multipleFailures),
                        getLiteralValue(aggregateScore),
                        getStringValue(isConfirmed),
                        getStringValue(originalText),
                        getStringValue(annotatedText));

                simpleWriter.append("\n");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return ExecutionContextFactory.createContext(inputRDF);
    }

    private String getMultipleObjectValues(StmtIterator iterator) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (iterator.hasNext()){
            Statement st = iterator.next();
            if (i > 0) sb.append("; ");
            sb.append(getLiteralValue(st));
            i++;
        }
        return sb.toString();
    }

    private void writeStringsIntoRow(BufferedWriter simpleWriter, String value1, String value2, String value3,
                                     String value4, String value5, String value6, String value7, String value8
    ) throws IOException {
        simpleWriter.append(value1).append(",");
        simpleWriter.append(value2).append(",");
        simpleWriter.append(value3).append(",");
        simpleWriter.append(value4).append(",");
        simpleWriter.append(value5).append(",");
        simpleWriter.append(value6).append(",");
        simpleWriter.append(value7).append(",");
        simpleWriter.append(value8).append(",");
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {
        dataPrefix = getEffectiveValue(P_DATE_PREFIX).asLiteral().toString();
        fileOutputPath = getEffectiveValue(P_FILE_OUTPUT_PATH).asLiteral().toString();
    }

    private static Property getSpecificParameter(String localPropertyName) {
        return ResourceFactory.createProperty(TYPE_URI + "/" + localPropertyName);
    }

    private String getLiteralValue(Statement st){
        if (st == null) return "";
        RDFNode node = st.getObject();
        if(node == null) return "";
        return Optional.ofNullable(node.asNode().getLiteralValue().toString()).orElse("");
    }

    private String getStringValue(Statement st) {
        if (st == null) return "";
        RDFNode node = st.getObject();
        if(node == null){
            return "";
        }
        return Optional.ofNullable(node.toString()).orElse("");
    }

}
