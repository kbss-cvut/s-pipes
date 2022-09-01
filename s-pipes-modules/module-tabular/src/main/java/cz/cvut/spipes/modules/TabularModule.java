package cz.cvut.spipes.modules;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.query.TypedQuery;
import cz.cvut.spipes.config.ExecutionConfig;
import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.exception.ResourceNotFoundException;
import cz.cvut.spipes.exception.ResourceNotUniqueException;
import cz.cvut.spipes.modules.exception.TableSchemaException;
import cz.cvut.spipes.modules.model.*;
import cz.cvut.spipes.modules.util.BNodesTransformer;
import cz.cvut.spipes.modules.util.JopaPersistenceUtils;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StreamResourceRegistry;
import cz.cvut.spipes.util.JenaUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.ResourceUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;

/**
 * Module for converting tabular data (e.g. CSV or TSV) to RDF
 * <p>
 * The implementation loosely follows the W3C Recommendation described here:
 * <a href="https://www.w3.org/TR/csv2rdf/">Generating RDF from Tabular Data on the Web</a>
 * <p>
 * Within the recommendation, it is possible to define schema
 * defining the shape of the output RDF data
 * (i.e. the input metadata values used for the conversion)
 * using csvw:tableSchema.<br/>
 * By default, we use the following schema:
 * <pre><code>
 * [   a   csvw:Table ;
 *     csvw:tableSchema
 *         [   a   csvw:TableSchema ;
 *             csvw:aboutUrl
 *                 "http://csv-resource-uri#row-{_row}"^^csvw:uriTemplate ;
 *             csvw:column
 *                 _:b0 , _:b1 , _:b2 ;
 *             csvw:columns
 *                 ( _:b0
 *                   _:b1
 *                   _:b2
 *                 )
 *         ]
 * ]
 * </code></pre>
 * <p>
 * <b>Important notes (differences from the recommendation):</b><br/>
 * Does not support custom table group URIs.<br/>
 * Does not support custom table URIs. <br/>
 * Does not support processing of multiple files.<br/>
 * Does not support the <i>suppress output</i> annotation.
 */
public class TabularModule extends AbstractModule {

    public static final String TYPE_URI = KBSS_MODULE.uri + "tabular";
    private static final Logger LOG = LoggerFactory.getLogger(TabularModule.class);

    private final Property P_DELIMITER = getSpecificParameter("delimiter");
    private final Property P_QUOTE_CHARACTER = getSpecificParameter("quote-character");
    private final Property P_DATE_PREFIX = getSpecificParameter("data-prefix");
    private final Property P_OUTPUT_MODE = getSpecificParameter("output-mode");
    private final Property P_SOURCE_RESOURCE_URI = getSpecificParameter("source-resource-uri");
    private final Property P_SKIP_HEADER = getSpecificParameter("skip-header");

    //sml:replace
    private boolean isReplace;

    //:source-resource-uri
    private StreamResource sourceResource;

    //:delimiter
    private int delimiter;

    //:quote-character
    private char quoteCharacter;

    //:data-prefix
    private String dataPrefix;

    //:skip-header
    private boolean skipHeader;

    //:output-mode
    private Mode outputMode;

    private Model outputModel;

    /**
     * Represent a group of tables.
     */
    private TableGroup tableGroup;

    /**
     * Represent a table.
     */
    private Table table;

    /**
     * Represents the table schema that was used to describe the table
     */
    private TableSchema inputTableSchema;

    @Override
    ExecutionContext executeSelf() {
        BNodesTransformer bNodesTransformer = new BNodesTransformer();
        Model inputModel = bNodesTransformer.convertBNodesToNonBNodes(executionContext.getDefaultModel());

        outputModel = ModelFactory.createDefaultModel();
        EntityManager em = JopaPersistenceUtils.getEntityManager("cz.cvut.spipes.modules.model", inputModel);
        em.getTransaction().begin();

        // TODO add references to specification (they are already in git somewhere!!!)
        tableGroup = onTableGroup(null);
        table = onTable(null);

        List<Column> outputColumns = new ArrayList<>();
        TableSchema tableSchema = table.getTableSchema();

        CsvPreference csvPreference = new CsvPreference.Builder(
            quoteCharacter,
            delimiter,
            "\\n").build();

        try{
            ICsvListReader listReader = new CsvListReader(getReader(), csvPreference);
            String[] header = listReader.getHeader(true); // skip the header (can't be used with CsvListReader)

            if (header == null) {
                LOG.warn("Input stream resource {} to provide tabular data is empty.", this.sourceResource.getUri());
                return getExecutionContext(inputModel, outputModel);
            }
            Set<String> columnNames = new HashSet<>();

            this.inputTableSchema = getTableSchema(em);
            if(skipHeader){
                header = getHeaderFromSchema(inputModel, header, inputTableSchema != null ? 1 : 0);
                listReader = new CsvListReader(getReader(), csvPreference);
            }else if (inputTableSchema != null) {
                header = getHeaderFromSchema(inputModel, header, 1);
            }


            String mainErrorMsg = "CSV table schema is not compliant with provided custom schema.";
            if (inputTableSchema != null && header.length != inputTableSchema.getColumnsSet().size()) {

                String mergedMsg = mainErrorMsg + "\n" +
                        "The number of columns in the table schema does not match the number of columns in the table." + "\n"
//                        .append(evidence).append("\n") TODO: rdf triples of evidence
                        ;
                logError(mergedMsg);
            }

            em = JopaPersistenceUtils.getEntityManager("cz.cvut.spipes.modules.model", outputModel);
            em.getTransaction().begin();

            List<Column> schemaColumns = new ArrayList<>(header.length);
            outputColumns = new ArrayList<>(header.length);

            int j = 0;
            for (String columnTitle : header) {
                String columnName = normalize(columnTitle);
                boolean isDuplicate = !columnNames.add(columnName);
                if(inputTableSchema != null) checkMissingColumns(mainErrorMsg, schemaColumns, columnName);

                Column column = new Column(columnName, columnTitle);
                outputColumns.add(column);

                setColumnAboutUrl(inputTableSchema != null, tableSchema, schemaColumns, j, column);

                Column schemaColumn = inputTableSchema != null ? schemaColumns.get(j) : null;
                String propertyUrl = getColumnPropertyUrl(inputTableSchema != null, schemaColumn, columnName);
                column.setProperty(propertyUrl);
                column.setPropertyUrl(propertyUrl);
                if(isDuplicate) throwNotUniqueException(column,columnTitle, columnName);
                j++;
            }
            tableSchema.getColumnsSet().addAll(outputColumns);

            List<String> row;
            int rowNumber = 0;
            //for each row
            while( (row = listReader.read()) != null ) {
                rowNumber++;
                // 4.6.1 and 4.6.3
                Row r = new Row();

                if (outputMode == Mode.STANDARD) {
                    // 4.6.2
                    table.getRows().add(r);
                    // 4.6.4
                    r.setRownum(rowNumber);
                    // 4.6.5
                    r.setUrl(sourceResource.getUri() + "#row=" + (rowNumber + 1));
                }

                // 4.6.6 - Add titles.
                // We do not support titles.

                // 4.6.7
                // In standard mode only, emit the triples generated by running
                // the algorithm specified in section 6. JSON-LD to RDF over any
                // non-core annotations specified for the row, with node R as
                // an initial subject, the non-core annotation as property, and the
                // value of the non-core annotation as value.

                for (int i = 0; i < header.length; i++) {
                    // 4.6.8.1
                    String columnAboutUrlStr = setColumnAboutUrl(outputColumns, tableSchema, rowNumber, i);
                    setValueUrl(inputModel, outputColumns, header, row, i, columnAboutUrlStr);
                    // 4.6.8.2
                    r.setDescribes(columnAboutUrlStr);
                    //TODO: URITemplate

                    // 4.6.8.5 - else, if value is list and cellOrdering == true
                    // 4.6.8.6 - else, if value is list
                    // 4.6.8.7 - else, if cellValue is not null
                }
            }

        } catch (IOException e) {
            LOG.error("Error while reading file from resource uri {}", sourceResource, e);
        }

        table.getTableSchema().setAboutUrl(null); // will be replaced by JOPA later
        em.persist(tableGroup);
        em.getTransaction().commit();

        Model persistenceModel = JopaPersistenceUtils.getDataset(em).getDefaultModel();
        persistenceModel.getResource(tableSchema.getUri().toString())
                .addLiteral(persistenceModel.getProperty(CSVW.aboutUrlUri),
                        persistenceModel.createTypedLiteral(sourceResource.getUri() + "#row-{_row}", CSVW.uriTemplate)
                );

        addColumnsList(em, outputColumns, tableSchema);

        outputModel.add(
                bNodesTransformer.transferJOPAEntitiesToBNodes
                        (JopaPersistenceUtils.getDataset(em).getDefaultModel()));

        em.close();
        return getExecutionContext(inputModel, outputModel);
    }

    private void setValueUrl(Model inputModel, List<Column> outputColumns, String[] header,
                             List<String> row, int i, String columnAboutUrlStr) {
        String columnPropertyUrl = outputColumns.get(i).getPropertyUrl();

        Column column = null;
        if (inputTableSchema != null){
            column = getColumnFromTableSchema(header[i], inputTableSchema);
        }
        String valueUrl = null;
        if(column != null) valueUrl = column.getValueUrl();

        if (valueUrl != null && !valueUrl.isEmpty()) {
            column.setValueUrl(valueUrl);
        } else {
            final String cellValue = row.get(i);
            if (cellValue != null) {
                Resource columnResource = ResourceFactory.createResource(columnAboutUrlStr);
                outputModel.add(ResourceFactory.createStatement(
                    columnResource,
                    inputModel.createProperty(columnPropertyUrl),
                    ResourceFactory.createPlainLiteral(cellValue)));
            }
        }
    }

    @NotNull
    private static String setColumnAboutUrl(List<Column> outputColumns, TableSchema tableSchema, int rowNumber, int i) {
        String columnAboutUrlStr = tableSchema.getAboutUrl();
        if (columnAboutUrlStr == null) columnAboutUrlStr = outputColumns.get(i).getAboutUrl();
        columnAboutUrlStr = columnAboutUrlStr.replace(
                "{_row}",
                Integer.toString(rowNumber + 1)
        );
        return columnAboutUrlStr;
    }

    private void setColumnAboutUrl(boolean hasTableSchema, TableSchema tableSchema, List<Column> schemaColumns,
                                   int j, Column column) {
        String columnAboutUrl = null;
        if(hasTableSchema && schemaColumns.get(j).getAboutUrl() != null)
            columnAboutUrl = schemaColumns.get(j).getAboutUrl();

        if (columnAboutUrl != null && !columnAboutUrl.isEmpty()) {
            column.setAboutUrl(columnAboutUrl);
        } else {
            tableSchema.setAboutUrl(sourceResource.getUri() + "#row-{_row}");
        }
    }

    private TableSchema getTableSchema(EntityManager em) {
        TypedQuery<TableSchema> query = em.createNativeQuery(
                "PREFIX csvw: <http://www.w3.org/ns/csvw#>\n" +
                        "SELECT ?t WHERE { \n" +
                        "?t a csvw:TableSchema. \n" +
                        "}",
                TableSchema.class
        );

        int tableSchemaCount = query.getResultList().size();

        if(tableSchemaCount > 1) {
            LOG.warn("More than one table schema found. Ignoring schemas {}. ", query.getResultList());
            return null;
        }
        if(tableSchemaCount == 0) {
            LOG.debug("No custom table schema found.");
            return null;
        }
        LOG.debug("Custom table schema found.");
        return query.getSingleResult();
    }

    private String getColumnPropertyUrl(boolean hasTableSchema,
                                        Column schemaColumn,
                                      String columnName) throws UnsupportedEncodingException {;
        if (hasTableSchema) {
            if (schemaColumn.getPropertyUrl() != null) {
                return schemaColumn.getPropertyUrl();
            }
            if (schemaColumn.getProperty() != null) {
                return schemaColumn.getProperty();
            }
        }
        if (dataPrefix != null && !dataPrefix.isEmpty()) {
            return dataPrefix + URLEncoder.encode(columnName, "UTF-8");
        }
        return sourceResource.getUri() + "#" + URLEncoder.encode(columnName, "UTF-8");
    }

    private void throwNotUniqueException(Column column, String columnTitle, String columnName) {
        throw new ResourceNotUniqueException(
                String.format("Unable to create value of property %s due to collision. " +
                        "Both column titles '%s' and '%s' are normalized to '%s' " +
                        "and thus would refer to the same property url <%s>.",
                        CSVW.propertyUrl,
                        columnTitle,
                        column.getTitle(),
                        columnName,
                        column.getPropertyUrl()));
    }

    private void checkMissingColumns(String mainErrorMsg, List<Column> schemaColumns,
                                     String columnTitle) {
        Column schemaColumn = getColumnFromTableSchema(columnTitle, inputTableSchema);
        schemaColumns.add(schemaColumn);
        if (schemaColumn == null) {
            String mergedMsg = mainErrorMsg + "\n" +
                    "Column with name '" + columnTitle + "' is missing." + "\n"
//                        .append(evidence).append("\n") TODO: rdf triples of evidence
                    ;

            logError(mergedMsg);
        }
    }

    private void addColumnsList(EntityManager em, List<Column> outputColumns, TableSchema tableSchema) {
        Model persistenceModel = JopaPersistenceUtils.getDataset(em).getDefaultModel();
        RDFNode[] elements = new RDFNode[outputColumns.size()];

        for (int i = 0; i < outputColumns.size(); i++) {
            Resource n = persistenceModel.getResource(outputColumns.get(i).getUri().toString());
            elements[i] = n;
        }

        RDFList columnList = persistenceModel.createList(elements);
        Resource convertedTableSchema =
                ResourceUtils.renameResource(persistenceModel.getResource(tableSchema.getUri().toString()), null);

        persistenceModel.add(
                convertedTableSchema,
                CSVW.columns,
                columnList);
    }

    private ExecutionContext getExecutionContext(Model inputModel, Model outputModel) {
        if (isReplace) {
            return ExecutionContextFactory.createContext(outputModel);
        } else {
            return ExecutionContextFactory.createContext(JenaUtils.createUnion(inputModel, outputModel));
        }
    }

    @Override
    public void loadConfiguration() {
        isReplace = getPropertyValue(SML.replace, false);
        delimiter = getPropertyValue(P_DELIMITER, '\t');
        skipHeader = getPropertyValue(P_SKIP_HEADER, false);
        quoteCharacter = getPropertyValue(P_QUOTE_CHARACTER, '\'');
        dataPrefix = getEffectiveValue(P_DATE_PREFIX).asLiteral().toString();
        sourceResource = getResourceByUri(getEffectiveValue(P_SOURCE_RESOURCE_URI).asLiteral().toString());
        outputMode = Mode.fromResource(
            getPropertyValue(P_OUTPUT_MODE, Mode.STANDARD.getResource())
        );
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    private static Property getSpecificParameter(String localPropertyName) {
        return ResourceFactory.createProperty(TYPE_URI + "/" + localPropertyName);
    }

    private TableGroup onTableGroup(String tableGroupUri) {
        // 1
        if (outputMode == Mode.STANDARD ) {
            // 2
            tableGroup = new TableGroup();
            if (tableGroupUri != null && !tableGroupUri.isEmpty()){
                tableGroup.setUri(URI.create(tableGroupUri));
            }
            // 3
            // In standard mode only, emit the triples generated by running
            // the algorithm specified in section 6. JSON-LD to RDF over any
            // notes and non-core annotations specified for the group of tables,
            // with node G as an initial subject, the notes or non-core
            // annotation as property, and the value
            // of the notes or non-core annotation as value.
        }
        return tableGroup;
    }

    private Table onTable(String tableUri) {
        // 4 - we never skip table
        if (outputMode == Mode.STANDARD) {
            // 4.1 and 4.3
            table = new Table();
            // 4.2
            tableGroup.setTable(table);
            // 4.4
            table.setUrl(sourceResource.getUri());
            if (tableUri != null && !tableUri.isEmpty()) {
                table.setUri(URI.create(tableUri));
            }

            // 4.5
            // In standard mode only, emit the triples generated by running
            // the algorithm specified in section 6. JSON-LD to RDF over any
            // notes and non-core annotations specified for the table, with
            // node T as an initial subject, the notes or non-core
            // annotation as property, and the value
            // of the notes or non-core annotation as value.
            TableSchema tableSchema = new TableSchema();
            table.setTableSchema(tableSchema);
        }

        return table;
    }

    private String normalize(String label) {
        return label.trim().replaceAll("[^\\w]", "_");
    }

    private Resource getColumnByName(String columnName) {
        return outputModel.listStatements(
                null,
                CSVW.name,
                ResourceFactory.createStringLiteral(columnName)
        ).next().getSubject();
    }

    private Reader getReader() {
        return new StringReader(new String(sourceResource.getContent()));
    }

    @NotNull
    private StreamResource getResourceByUri(@NotNull String resourceUri) {

        StreamResource res = StreamResourceRegistry.getInstance().getResourceByUrl(resourceUri);

        if (res == null) {
            throw new ResourceNotFoundException("Stream resource " + resourceUri + " not found. ");
        }
        return res;
    }

    public boolean isReplace() {
        return isReplace;
    }

    public void setReplace(boolean replace) {
        isReplace = replace;
    }

    public StreamResource getSourceResource() {
        return sourceResource;
    }

    public void setSourceResource(StreamResource sourceResource) {
        this.sourceResource = sourceResource;
    }

    public int getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(int delimiter) {
        this.delimiter = delimiter;
    }

    public char getQuote() {
        return quoteCharacter;
    }

    public void setQuoteCharacter(char quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
    }

    public String getDataPrefix() {
        return dataPrefix;
    }

    public void setDataPrefix(String dataPrefix) {
        this.dataPrefix = dataPrefix;
    }

    public Mode getOutputMode() {
        return outputMode;
    }

    public void setOutputMode(Mode outputMode) {
        this.outputMode = outputMode;
    }

    public void setSkipHeader(boolean skipHeader) {
        this.skipHeader = skipHeader;
    }

    private String[] getHeaderFromSchema(Model inputModel, String[] header, int tableSchemaCount) {
        if (tableSchemaCount == 1) {
            List<String> orderList = new ArrayList<>();
            Resource tableSchemaResource = inputModel.getResource(inputTableSchema.getUri().toString());
            Statement statement = tableSchemaResource.getProperty(CSVW.columns);

            if (statement != null) {
                RDFNode node = statement.getObject();
                RDFList rdfList = node.as(RDFList.class);

                rdfList.iterator().forEach(rdfNode -> orderList.add(String.valueOf(rdfNode)));
                header = createHeaders(header.length, inputTableSchema.sortColumns(orderList));

            } else LOG.info("Order of columns was not provided in the schema.");
        } else {
            header = createHeaders(header.length, new ArrayList<>());
        }
        return header;
    }

    private String[] createHeaders(int size, List<Column> columns) {
        String[] headers = new String[size];

        for(int i = 0; i < size; i++){
            if(!columns.isEmpty()){
                headers[i] = columns.get(i).getName();
            }else headers[i] = "column_" + (i + 1);
        }
        return headers;
    }

    private Column getColumnFromTableSchema(String columnName, TableSchema tableSchema) {
        for (Column column : tableSchema.getColumnsSet()) {
            if (column.getName() != null && column.getName().equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    private void logError(String msg) {
        if (ExecutionConfig.isExitOnError()) {
            throw new TableSchemaException(msg, this);
        }else LOG.error(msg);
    }
}
