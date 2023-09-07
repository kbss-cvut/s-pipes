package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.constants.KBSS_CSVW;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Module for converting RDF (representing table) to CSV
 * <p>
 * The module is responsible for converting the input RDF data into a CSV format and saving the output to a file.
 * The table is constructed from column and row resources defined in TableSchema and saves it as a new CSV file.
 * </p>
 */
@SPipesModule(label = "RDF2CSV", comment = "Module for converting RDF (representing table) to CSV. " +
        "The module is responsible for converting the input RDF data into a CSV format and saving the output to a file." +
        "The table is constructed from column and row resources defined in TableSchema and saves it as a new CSV file.")
public class RDF2CSVModule extends AnnotatedAbstractModule {

    public static final String TYPE_URI = KBSS_MODULE.uri + "RDF2CSV";
    public static final String TYPE_PREFIX = TYPE_URI + "/";

    private static final Logger LOG = LoggerFactory.getLogger(RDF2CSVModule.class);

    /** The parameter representing where the output file will be stored */
    @Parameter(urlPrefix = TYPE_PREFIX, name = "file-output-path")
    private String fileOutputPath;

    @Override
    ExecutionContext executeSelf(){
        Model inputRDF = this.getExecutionContext().getDefaultModel();

        try(CsvListWriter simpleWriter = new CsvListWriter
                (new FileWriter(fileOutputPath, false),
                        CsvPreference.STANDARD_PREFERENCE)
        ){

            Resource table =  inputRDF.listResourcesWithProperty(RDF.type, CSVW.Table)
                    .next();
            if (table == null) {
                LOG.warn("No Table resource found in the input RDF.");
                return ExecutionContextFactory.createContext(inputRDF);
            }

            Resource tableSchema = table.getProperty(CSVW.tableSchema).getObject().asResource();

            if (tableSchema == null) {
                LOG.warn("No TableSchema resource found in the input RDF.");
                return ExecutionContextFactory.createContext(inputRDF);
            }

            Statement columnsStatement = tableSchema.getProperty(CSVW.columns);
            if (columnsStatement == null) {
                LOG.warn("Columns statement not found in the table schema.");
                return ExecutionContextFactory.createContext(inputRDF);
            }

            Resource columnsList = columnsStatement.getObject().asResource();
            RDFList columns = columnsList.as(RDFList.class);
            if (columns == null || columns.isEmpty()) {
                LOG.warn("Columns list not found or is empty in the columns statement.");
                return ExecutionContextFactory.createContext(inputRDF);
            }

            List<String> header = columns.asJavaList().stream()
                    .map(rdfNode -> {
                        Resource columnResource = rdfNode.asResource();
                        Statement nameStatement = columnResource.getProperty(CSVW.name);
                        if (nameStatement == null) {
                            LOG.warn("Name property not found for column resource.");
                            return "";
                        }
                        RDFNode titleNode = nameStatement.getObject();
                        if (titleNode == null) {
                            LOG.warn("Name node not found in the name statement.");
                            return "";
                        }
                        return titleNode.toString();
                    })
                    .collect(Collectors.toList());

            simpleWriter.write(header);

            List<RDFNode> rowList = table.listProperties(CSVW.row)
                    .mapWith(Statement::getObject)
                    .toList();

            rowList.sort(Comparator.comparingInt(o -> o.asResource().getProperty(CSVW.rowNum).getInt()));

            for (RDFNode node: rowList){
                List<String> row = new ArrayList<>();
                Resource rowResource = node.asResource();
                Resource res = rowResource.getProperty(CSVW.describes).getObject().asResource();

                for (RDFNode col : columns.asJavaList()) {
                    Property property = inputRDF.getProperty(col.asResource().getProperty(KBSS_CSVW.property).getObject().toString());
                    row.add(res.hasProperty(property) ? getObjectValueFromStatement(res.getProperty(property)) : "");
                }
                simpleWriter.write(row);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return ExecutionContextFactory.createContext(inputRDF);
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    private String getObjectValueFromStatement(Statement st){
        if (st == null) return "";
        RDFNode node = st.getObject();
        if(node == null) return "";

        return node.isLiteral()
                ? Optional.ofNullable(node.asNode().getLiteralValue().toString()).orElse("")
                : Optional.ofNullable(node.toString()).orElse("");
    }
}
