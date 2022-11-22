package cz.cvut.spipes.modules;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.query.TypedQuery;
import cz.cvut.spipes.InvalidQuotingTokenizer;
import cz.cvut.spipes.config.ExecutionConfig;
import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.exception.ResourceNotFoundException;
import cz.cvut.spipes.exception.ResourceNotUniqueException;
import cz.cvut.spipes.modules.model.*;
import cz.cvut.spipes.modules.util.BNodesTransformer;
import cz.cvut.spipes.modules.util.JopaPersistenceUtils;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StreamResourceRegistry;
import cz.cvut.spipes.util.JenaUtils;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.jena.rdf.model.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Module for converting tabular data (e.g. CSV or TSV) to RDF
 * <p>
 * It supports two major processing standards that can be set by separator:
 * <ul><li> separator ',' -- defaults to
 * <a href="https://www.rfc-editor.org/rfc/rfc4180">CSV standard</a>, i.e. it uses by default quoting " and UTF-8 </li>
 * <li> separator '\t' -- defaults to
 * <a href="https://www.iana.org/assignments/media-types/text/tab-separated-values">TSV standard</a>, with no quoting
 * (In the TSV standard, there is no mention of quotes, but in this implementation, we process the TSV quotes
 * the same way as the CSV quotes.)</li>
 * <li> other separator -- defaults to no standard, with no quoting</li>
 * </ul>
 * </p>
 * In addition, it supports bad quoting according to CSV standard, see option
 * {@link TabularModule#acceptInvalidQuoting}
 * and class {@link InvalidQuotingTokenizer}
 * <p>The implementation loosely follows the W3C Recommendation described here:
 * <a href="https://www.w3.org/TR/csv2rdf/">Generating RDF from Tabular Data on the Web</a></p>
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
    private final Property P_ACCEPT_INVALID_QUOTING = getSpecificParameter("accept-invalid-quoting");
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

    //:accept-invalid-quoting
    private boolean acceptInvalidQuoting;

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
    private TableSchema tableSchema;

    @Override
    ExecutionContext executeSelf() {
        BNodesTransformer bNodesTransformer = new BNodesTransformer();
        Model inputModel = bNodesTransformer.convertBNodesToNonBNodes(executionContext.getDefaultModel());
        boolean hasInputSchema = false;

        Model outputModel = ModelFactory.createDefaultModel();
        EntityManager em = JopaPersistenceUtils.getEntityManager("cz.cvut.spipes.modules.model", inputModel);
        em.getTransaction().begin();

        tableGroup = onTableGroup(null);
        table = onTable(null);

        List<Column> outputColumns = new ArrayList<>();
        List<Statement> rowStatements = new ArrayList<>();

        CsvPreference csvPreference = new CsvPreference.Builder(
                quoteCharacter,
                delimiter,
                System.lineSeparator()).build();

        try{
            ICsvListReader listReader = getCsvListReader(csvPreference);

            if (listReader == null) {
                logMissingQuoteError();
                return getExecutionContext(inputModel, outputModel);
            }

            String[] header = listReader.getHeader(true); // skip the header (can't be used with CsvListReader)

            if (header == null) {
                LOG.warn("Input stream resource {} to provide tabular data is empty.", this.sourceResource.getUri());
                return getExecutionContext(inputModel, outputModel);
            }
            Set<String> columnNames = new HashSet<>();

            TableSchema inputTableSchema = getTableSchema(em);
            hasInputSchema = hasInputSchema(inputTableSchema);

            if(skipHeader){
                header = getHeaderFromSchema(inputModel, header, hasInputSchema);
                listReader = new CsvListReader(getReader(), csvPreference);
            }else if (hasInputSchema) {
                header = getHeaderFromSchema(inputModel, header, true);
            }

            em = JopaPersistenceUtils.getEntityManager("cz.cvut.spipes.modules.model", outputModel);
            em.getTransaction().begin();

            outputColumns = new ArrayList<>(header.length);

            for (String columnTitle : header) {
                String columnName = normalize(columnTitle);
                boolean isDuplicate = !columnNames.add(columnName);

                Column schemaColumn = new Column(columnName, columnTitle);
                outputColumns.add(schemaColumn);

                tableSchema.setAboutUrl(schemaColumn, sourceResource.getUri());
                schemaColumn.setProperty(
                        dataPrefix,
                        sourceResource.getUri(),
                        hasInputSchema ? tableSchema.getColumn(columnName) : null);
                schemaColumn.setTitle(columnTitle);
                if(isDuplicate) throwNotUniqueException(schemaColumn,columnTitle, columnName);
            }

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
                    Column column = outputColumns.get(i);
                    String cellValue = row.get(i);
                    if (cellValue != null) rowStatements.add(createRowResource(cellValue, rowNumber, column));
                    // 4.6.8.2
                    r.setDescribes(tableSchema.createAboutUrl(rowNumber));
                    //TODO: URITemplate

                    // 4.6.8.5 - else, if value is list and cellOrdering == true
                    // 4.6.8.6 - else, if value is list
                    // 4.6.8.7 - else, if cellValue is not null
                }
            }

        } catch (IOException | MissingArgumentException e) {
            LOG.error("Error while reading file from resource uri {}", sourceResource, e);
        }

        tableSchema.adjustProperties(hasInputSchema, outputColumns, sourceResource.getUri());
        em.persist(tableGroup);

        tableSchema.setColumnsSet(new HashSet<>(outputColumns));
        em.merge(tableSchema);
        em.getTransaction().commit();
        tableSchema.addColumnsList(em, outputColumns);

        outputModel.add(rowStatements);
        outputModel.add(
                bNodesTransformer.transferJOPAEntitiesToBNodes
                        (JopaPersistenceUtils.getDataset(em).getDefaultModel()));
        em.close();
        return getExecutionContext(inputModel, outputModel);
    }

    private ICsvListReader getCsvListReader(CsvPreference csvPreference) {
        if (acceptInvalidQuoting) {
            if (getQuote() == '\0') {
                return null;
            }else
                return new CsvListReader(new InvalidQuotingTokenizer(getReader(), csvPreference), csvPreference);
        }
        return new CsvListReader(getReader(), csvPreference);
    }

    private Statement createRowResource(String cellValue, int rowNumber, Column column) {
        Resource rowResource = ResourceFactory.createResource(tableSchema.createAboutUrl(rowNumber));

        return ResourceFactory.createStatement(
                rowResource,
                ResourceFactory.createProperty(column.getPropertyUrl()),
                ResourceFactory.createPlainLiteral(cellValue));
    }

    private boolean hasInputSchema(TableSchema inputTableSchema) {
        if (inputTableSchema != null){
            tableSchema = inputTableSchema;
            table.setTableSchema(tableSchema);
            return true;
        }
        return false;
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
        delimiter = getPropertyValue(P_DELIMITER, '\0');
        skipHeader = getPropertyValue(P_SKIP_HEADER, false);
        acceptInvalidQuoting = getPropertyValue(P_ACCEPT_INVALID_QUOTING, false);
        quoteCharacter = getPropertyValue(P_QUOTE_CHARACTER, '\0');
        dataPrefix = getEffectiveValue(P_DATE_PREFIX).asLiteral().toString();
        sourceResource = getResourceByUri(getEffectiveValue(P_SOURCE_RESOURCE_URI).asLiteral().toString());
        outputMode = Mode.fromResource(
                getPropertyValue(P_OUTPUT_MODE, Mode.STANDARD.getResource())
        );

        if(delimiter == '\0'){
            delimiter = ',';
            quoteCharacter = '"';
            LOG.debug("Using default values for CSV, i.e. delimiter = ',' and quote character = '\"' and UTF-8");
        }
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
            tableSchema = new TableSchema();
            table.setTableSchema(tableSchema);
        }

        return table;
    }

    private String normalize(String label) {
        return label.trim().replaceAll("[^\\w]", "_");
    }

    private Reader getReader() {
        return new StringReader(
                delimiter == ','
                        ? new String(sourceResource.getContent(), StandardCharsets.UTF_8)
                        : new String(sourceResource.getContent()));
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

    private String[] getHeaderFromSchema(Model inputModel, String[] header, boolean hasInputSchema) {
        if (hasInputSchema) {
            List<String> orderList = new ArrayList<>();
            Resource tableSchemaResource = inputModel.getResource(tableSchema.getUri().toString());
            Statement statement = tableSchemaResource.getProperty(CSVW.columns);

            if (statement != null) {
                RDFNode node = statement.getObject();
                RDFList rdfList = node.as(RDFList.class);

                rdfList.iterator().forEach(rdfNode -> orderList.add(String.valueOf(rdfNode)));
                tableSchema.setOrderList(orderList);
                header = createHeaders(header.length, tableSchema.sortColumns(orderList));

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

    private void logMissingQuoteError() throws MissingArgumentException {
        String message = "Quote character must be specified when using custom tokenizer.";
        if (ExecutionConfig.isExitOnError()) {
            throw new MissingArgumentException(message);
        }else LOG.error(message);
    }
}
