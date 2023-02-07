package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.*;

public class RDF2CSVModule extends AnnotatedAbstractModule {

    public static final String TYPE_URI = KBSS_MODULE.uri + "RDF2CSV";
    public static final String TYPE_PREFIX = TYPE_URI + "/";

    private static final Logger LOG = LoggerFactory.getLogger(RDF2CSVModule.class);

    @Parameter(urlPrefix = TYPE_PREFIX, name = "data-prefix")
    private String dataPrefix;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "file-output-path")
    private String fileOutputPath;

    @Override
    ExecutionContext executeSelf(){
        Model inputRDF = this.getExecutionContext().getDefaultModel();

        try(CsvListWriter simpleWriter = new CsvListWriter
                (new FileWriter(fileOutputPath, false),
                        CsvPreference.STANDARD_PREFERENCE)
        ){
            List<String> header = Arrays.asList( "DocumentId","DocumentLineNumber", "WorkOrderId","TaskCardId",
                    "ComponentURI","ComponentLabel","ComponentScore","MultipleComponents"
                    ,"FailureURI","FailureLabel","FailureScore","MultipleFailures",
                    "AggregateScore","IsConfirmed" ,"OriginalText","AnnotatedText");
            simpleWriter.write(header);

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

                List<String> row = Arrays.asList(
                        getStringValue(docId),
                        getStringValue(lineNumber),
                        getStringValue(woID),
                        getStringValue(tcID),
                        getStringValue(compUri),
                        getLiteralValue(compLabel),
                        getLiteralValue(compScore),
                        getMultipleObjectValues(multipleComps),
                        getStringValue(failureUri),
                        getLiteralValue(failureLabel),
                        getLiteralValue(failureScore),
                        getMultipleObjectValues(multipleFailures),
                        getLiteralValue(aggregateScore),
                        getStringValue(isConfirmed),
                        getStringValue(originalText),
                        StringEscapeUtils.unescapeJava(getStringValue(annotatedText))
                );

                simpleWriter.write(row);
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

    @Override
    public String getTypeURI() {
        return TYPE_URI;
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
