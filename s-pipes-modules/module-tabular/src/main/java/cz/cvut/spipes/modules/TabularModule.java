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
import cz.cvut.spipes.exception.SPipesException;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.modules.exception.MissingArgumentException;
import cz.cvut.spipes.modules.exception.SheetIsNotSpecifiedException;
import cz.cvut.spipes.modules.exception.SpecificationNonComplianceException;
import cz.cvut.spipes.modules.handlers.ModeHandler;
import cz.cvut.spipes.modules.model.*;
import cz.cvut.spipes.modules.util.*;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StreamResourceRegistry;
import cz.cvut.spipes.util.JenaUtils;
import org.apache.jena.rdf.model.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

/**
 * Module for converting input that contains tabular data (e.g. CSV, TSV, XLS, HTML) to RDF
 * <p>
 * It supports major processing standards that can be set by "resource format", with values :
 * <ul>
 * <li> "text/csv" -- <a href="https://www.rfc-editor.org/rfc/rfc4180">CSV standard</a>
 * with ',' as default separator,  the double-quote as a quote character, and UTF-8 as the encoding</li>
 * <li> "text/tab-separated-values" --
 * <a href="https://www.iana.org/assignments/media-types/text/tab-separated-values">TSV standard</a>
 * with ',' as default separator and no quoting (In the TSV standard, fields that contain '\t' are not allowed
 * and there is no mention of quotes, but in this implementation, we process the TSV quotes
 * the same way as the CSV quotes.)</li>
 * <li> other resource formats -- defaults to no standard, with no quoting</li>
 * </ul>
 * </p>
 * In addition, it supports bad quoting according to CSV standard, see option
 * {@link TabularModule#acceptInvalidQuoting}
 * and class {@link InvalidQuotingTokenizer}
 * <p>The implementation loosely follows the W3C Recommendation described here:
 * <a href="https://www.w3.org/TR/csv2rdf/">Generating RDF from Tabular Data on the Web</a></p>
 * <p>
 * Within the recommendation, it is possible to specify schema
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
 * Table schema can be provided in the input RDF data ("input schema") and is also included in the output RDF data
 * ("output schema") of this module. If the input schema is provided, the output schema should consistently extend it.
 * Following situations can happen:
 * 1) there is no input schema in the input RDF data of this module
 *   a) {@link TabularModule#skipHeader} is false -- the output schema is created based on the header of the input file
 *   b) {@link TabularModule#skipHeader} is true -- the output schema is created based on number of columns,
 *      where column names "column_1", "column_2", etc.
 * 2) there is an input schema in the input RDF data of this module
 *   a) {@link TabularModule#skipHeader} is false -- the output schema is consistently extended from data. This is
 *      typically used when we have input data schema that does not define order of columns, while the output schema
 *      will be extended with this order based on the header of the input file.
 *   b) {@link TabularModule#skipHeader} is true -- the output schema is reused from the input RDF data
 *
 * <p>
 * This module can also be used to process HTML tables, see option {@link TabularModule#sourceResourceFormat}.
 * First, the HTML table is converted to TSV while replacing "\t" with two spaces
 * and then processed as usual.
 * Take a look at the option {@link TabularModule#sourceResourceFormat}.
 *
 * <p>
 * Tabular module converts cell values in HTML tables to plain text or html (preserves html tags) based on the <code>preserveTags</code>
 * attributes defined in the table and its elements. HTML tags in a cell value are preserved if a <code>preserveTags=true</code>
 * attribute is specified on the cell, row, column header or table element. Otherwise, cell value is converted to plain text.
 *
 * Here are some examples:
 * <ul>
 * <li> to convert all cell values in a table to plain text - no need to specify <code>preserveTags</code> anywhere</li>
 * <li> to preserve all tags in all cells - specify <code>preserveTags="true"</code> only on table element
 * <pre><code>
 * <table preserveTags="true">
 * ...
 * </table>
 * </code></pre>
 * </li>
 * <li> to preserve tags of cell values of a column (<code>Description</code>) - specify <code>preserveTags="true"</code> on the column (<code>Description</code>)
 * <pre><code>
 * <table>
 * <ht><td>Id</td>  <td>Label</td>   <td preserveTags="true">Description</td></th>
 * <tr><td>1</td>   <td>Sugar</td>   <td><span>Sugar</span> is sweet</td></tr>
 * <tr><td>2</td>   <td>Salt</td>    <td><a href="...">Salt</a> is salty</td></tr>
 * ...
 * </table>
 * </code></pre>
 * </li>
 * <li> to preserve tags for all rows in a column (<code>Description</code>) except for cell at row 2 - specify <code>preserveTags="true"</code>
 * on the column (<code>Description</code>) and <code>preserveTags="false"</code> on second row cell in the column (<code>Description</code>).
 * <pre><code>
 * <table>
 * <ht><td>Id</td>  <td>Label</td>   <td preserveTags="true">Description</td></th>
 * <tr><td>1</td>   <td>Sugar</td>   <td><span>Sugar</span> is sweet</td></tr>
 * <tr><td>2</td>   <td>Salt</td>    <td preserveTags="false"><a href="...">Salt</a> is salty</td></tr>
 * ...
 * </table>
 * </code></pre>
 * </li>
 * </ul>
 * Also, in a similar way this module can process XLS tables. Note, that processing multiple sheets isn't supported,
 * so {@link TabularModule#processTableAtIndex} parameter is required (range 1...number of sheets).
 * <p>
 * <b>Important notes (differences from the recommendation):</b><br/>
 * Does not support custom table group URIs.<br/>
 * Does not support custom table URIs. <br/>
 * Does not support processing of multiple files.<br/>
 * Does not support the <i>suppress output</i> annotation.
 */
@SPipesModule(label = "Tabular module", comment = "Module for converting tabular data (e.g. CSV or TSV) to RDF")
public class TabularModule extends AnnotatedAbstractModule {

    public static final String TYPE_URI = KBSS_MODULE.uri + "tabular";
    public static final String PARAM_URL_PREFIX = TYPE_URI + "/";
    private static final Logger LOG = LoggerFactory.getLogger(TabularModule.class);
    private final Property P_DELIMITER = getSpecificParameter("delimiter");
    private final Property P_QUOTE_CHARACTER = getSpecificParameter("quote-character");
    private final Property P_ACCEPT_INVALID_QUOTING = getSpecificParameter("accept-invalid-quoting");
    private final Property P_DATE_PREFIX = getSpecificParameter("data-prefix");
    private final Property P_OUTPUT_MODE = getSpecificParameter("output-mode");
    private final Property P_SOURCE_RESOURCE_URI = getSpecificParameter("source-resource-uri");
    private final Property P_SKIP_HEADER = getSpecificParameter("skip-header");
    private final Property P_SOURCE_RESOURCE_FORMAT = getSpecificParameter("source-resource-format");
    private final Property P_PROCESS_TABLE_AT_INDEX = getSpecificParameter("process-table-at-index");

    @Parameter(iri = SML.replace, comment = "Specifies whether a module should overwrite triples" +
            " from its predecessors. When set to true (default is false), it prevents" +
            " passing through triples from the predecessors.")
    private boolean isReplace = false;

    @Parameter(iri = PARAM_URL_PREFIX + "source-resource-uri", comment = "URI of resource" +
            " that represent tabular data (e.g. resource representing CSV file).")
    private StreamResource sourceResource;

    @Parameter(iri = PARAM_URL_PREFIX + "delimiter", comment = "Column delimiter. Default value is comma ','.")
    private int delimiter;

    @Parameter(iri = PARAM_URL_PREFIX + "quote-character", comment = "Quote character. Default is '\"' if delimiter is ',', '\\0' otherwize.")
    private char quoteCharacter;

    @Parameter(iri = PARAM_URL_PREFIX + "data-prefix", comment = "Data prefix")// TODO - improve comment
    private String dataPrefix;

    @Parameter(iri = PARAM_URL_PREFIX + "skip-header", comment = "Skip header. Default is false.")
    private boolean skipHeader = false;

    //:process-table-at-index
    /**
     * Required parameter for HTML and EXCEL files that indicates that only specific single table should be processed
     */
    @Parameter(iri = PARAM_URL_PREFIX + "process-table-at-index", comment = "Required parameter for HTML and EXCEL files that indicates that only specific single table should be processed")
    private int processTableAtIndex = 0;

    // TODO - revise comment
    @Parameter(iri = PARAM_URL_PREFIX + "output-mode", comment = "Output mode. Default is standard-mode('http://onto.fel.cvut.cz/ontologies/lib/module/tabular/standard-mode)", handler = ModeHandler.class)
    private Mode outputMode;

    //:source-resource-format
    /**
     * Parameter that indicates format of the source file.
     * Supported formats:
     * - "text/plain" -- plain text, default value.
     * - "text/csv" -- coma-separated values (csv).
     * - "text/tab-separated-values" -- tab-separated values (tsv).
     * - "text/html" -- HTML file.
     * - "application/vnd.ms-excel" - EXCEL (XLS) file.
     * - "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" - EXCEL (XLSX) file.
     * - "application/vnd.ms-excel.sheet.macroEnabled.12" - EXCEL (XLSM) file.
     */
    private ResourceFormat sourceResourceFormat = ResourceFormat.PLAIN;

    public void setAcceptInvalidQuoting(boolean acceptInvalidQuoting) {
        this.acceptInvalidQuoting = acceptInvalidQuoting;
    }

    @Parameter(iri = PARAM_URL_PREFIX + "accept-invalid-quoting", comment = "Accept invalid quoting. Default is false.")
    private boolean acceptInvalidQuoting = false;

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

    /**
     * Default charset to process input file.
     */
    private Charset inputCharset = Charset.defaultCharset();

    @Override
    ExecutionContext executeSelf() {

        tableGroup = onTableGroup(null);
        table = onTable(null);

        StreamReaderAdapter streamReaderAdapter;

        switch (sourceResourceFormat) {
            case HTML:
                if (processTableAtIndex == 0) {
                    throw new SheetIsNotSpecifiedException("Source resource format is set to HTML file but no specific table is set for processing.");
                }
                if (processTableAtIndex != 1) {
                    throw new UnsupportedOperationException("Support for 'process-table-at-index' different from 1 is not implemented for HTML files yet.");
                }
                streamReaderAdapter = new HTMLStreamReaderAdapter();
                break;
            case XLS:
            case XLSM:
            case XLSX:
                if (processTableAtIndex == 0) {
                    throw new SheetIsNotSpecifiedException("Source resource format is set to XLS(X,M) file but no specific table is set for processing.");
                }
                streamReaderAdapter = new XLSStreamReaderAdapter();
                break;
            default:
                streamReaderAdapter = new CSVStreamReaderAdapter(quoteCharacter, delimiter, acceptInvalidQuoting, inputCharset);
                break;
        }

        BNodesTransformer bNodesTransformer = new BNodesTransformer();
        Model inputModel = bNodesTransformer.convertBNodesToNonBNodes(executionContext.getDefaultModel());
        boolean hasInputSchema = false;

        Model outputModel = ModelFactory.createDefaultModel();
        EntityManager em = JopaPersistenceUtils.getEntityManager("cz.cvut.spipes.modules.model", inputModel);
        em.getTransaction().begin();

        List<Column> outputColumns = new ArrayList<>();
        List<Statement> rowStatements = new ArrayList<>();

        try {
            streamReaderAdapter.initialise(new ByteArrayInputStream(sourceResource.getContent()),
                sourceResourceFormat, processTableAtIndex, sourceResource);
            String[] header = streamReaderAdapter.getHeader(skipHeader);;
            Set<String> columnNames = new HashSet<>();

            if (streamReaderAdapter.getSheetLabel() != null) {
                table.setLabel(streamReaderAdapter.getSheetLabel());
            }

            TableSchema inputTableSchema = getTableSchema(em);
            hasInputSchema = hasInputSchema(inputTableSchema);

            if (skipHeader) {
                header = getHeaderFromSchema(inputModel, header, hasInputSchema);
            } else if (hasInputSchema) {
                header = getHeaderFromSchema(inputModel, header, true);
            }
            em.getTransaction().commit();
            em.close();
            em.getEntityManagerFactory().close();

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
                if (isDuplicate) {
                    throwNotUniqueException(schemaColumn, columnTitle, columnName);
                }
            }

            int rowNumber = 0;
            List<String> row;
            while ((row = streamReaderAdapter.getNextRow()) != null) {
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
                    String cellValue = getValueFromRow(row, i, header.length, rowNumber);
                    if (cellValue != null) rowStatements.add(createRowResource(cellValue, rowNumber, column));
                    // 4.6.8.2
                    r.setDescribes(tableSchema.createAboutUrl(rowNumber));
                    //TODO: URITemplate

                    // 4.6.8.5 - else, if value is list and cellOrdering == true
                    // 4.6.8.6 - else, if value is list
                    // 4.6.8.7 - else, if cellValue is not null
                }
            }

        tableSchema.adjustProperties(hasInputSchema, outputColumns, sourceResource.getUri());
        tableSchema.setColumnsSet(new HashSet<>(outputColumns));

        em = JopaPersistenceUtils.getEntityManager("cz.cvut.spipes.modules.model", outputModel);
        em.getTransaction().begin();
        em.persist(tableGroup);
        em.merge(tableSchema);

        List<Region> regions = streamReaderAdapter.getMergedRegions();

        int cellsNum = 1;
        for (Region region : regions) {
            int firstCellInRegionNum = cellsNum;
            URI firstCellInRegion = null;
            for (int i = region.getFirstRow(); i <= region.getLastRow(); i++) {
                for (int j = region.getFirstColumn(); j <= region.getLastColumn(); j++) {
                    Cell cell = new Cell(sourceResource.getUri() + "#cell" + cellsNum);
                    cell.setRow(tableSchema.createAboutUrl(i));
                    cell.setColumn(outputColumns.get(j).getUri());
                    if (cellsNum != firstCellInRegionNum) {
                        if(firstCellInRegion == null)
                            firstCellInRegion = URI.create(sourceResource.getUri() + "#cell" + firstCellInRegionNum);
                        cell.setSameValueAsCell(firstCellInRegion);
                    }
                    em.merge(cell);
                    cellsNum++;
                }
            }
        }
        streamReaderAdapter.close();
        } catch (MissingArgumentException e) {
                if (ExecutionConfig.isExitOnError()) {
                    return getExecutionContext(inputModel, outputModel);
                }
        } catch (IOException e) {
            LOG.error("Error while reading file from resource uri {}", sourceResource, e);
        }

        em.getTransaction().commit();
        Model persistedModel = JopaPersistenceUtils.getDataset(em).getDefaultModel();
        em.getEntityManagerFactory().close();

        tableSchema.addColumnsList(persistedModel, outputColumns);
        outputModel.add(rowStatements);
        outputModel.add(bNodesTransformer.transferJOPAEntitiesToBNodes(persistedModel));

        return getExecutionContext(inputModel, outputModel);
    }

    private String getValueFromRow(List<String> row, int index, int expectedRowLength, int currentRecordNumber) {
        try {
            return row.get(index);
        } catch (IndexOutOfBoundsException e) {
            String recordDelimiter = "\n----------\n";
            StringBuilder record = new StringBuilder(recordDelimiter);
            for (int i = 0; i < row.size(); i++) {
                record
                        .append(i)
                        .append(":")
                        .append(row.get(i))
                        .append(recordDelimiter);
            }
            LOG.error("Reading input file failed when reading record #{} (may not reflect the line #).\n" +
                            " It was expected that the current record contains {} values" +
                            ", but {}. element was not retrieved before whole record was processed.\n" +
                            "The problematic record: {}",
                    currentRecordNumber,
                    expectedRowLength,
                    index+1,
                    record
            );
            throw new SPipesException("Reading input file failed.", e);
        }
    }

    private Statement createRowResource(String cellValue, int rowNumber, Column column) {
        Resource rowResource = ResourceFactory.createResource(tableSchema.createAboutUrlStr(rowNumber));

        return ResourceFactory.createStatement(
                rowResource,
                ResourceFactory.createProperty(column.getPropertyUrl()),
                ResourceFactory.createPlainLiteral(cellValue));
    }

    private boolean hasInputSchema(TableSchema inputTableSchema) {
        if (inputTableSchema != null) {
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

        if (tableSchemaCount > 1) {
            LOG.warn("More than one table schema found. Ignoring schemas {}. ", query.getResultList());
            return null;
        }
        if (tableSchemaCount == 0) {
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
    public void loadManualConfiguration() {
        sourceResourceFormat = ResourceFormat.fromString(
                getPropertyValue(P_SOURCE_RESOURCE_FORMAT, ResourceFormat.PLAIN.getValue())
        );
        delimiter = getPropertyValue(P_DELIMITER, getDefaultDelimiterSupplier(sourceResourceFormat));
        quoteCharacter = getPropertyValue(P_QUOTE_CHARACTER, getDefaultQuoteCharacterSupplier(sourceResourceFormat));
        outputMode = Mode.fromResource(
                getPropertyValue(P_OUTPUT_MODE, Mode.STANDARD.getResource())
        );
        setInputCharset(delimiter);
    }


    private void setInputCharset(int delimiter) {
        if (delimiter == ',') {
            inputCharset = StandardCharsets.UTF_8;
            LOG.debug("Using UTF-8 as the encoding to be compliant with RFC 4180 (CSV)");
        }
    }

    private Supplier<Character> getDefaultDelimiterSupplier(ResourceFormat sourceResourceFormat) {
        if (sourceResourceFormat == ResourceFormat.CSV) {
            return () -> {
                LOG.debug("Using comma as default value of delimiter to be compliant with RFC 4180 (CSV).");
                return ',';
            };
        }
        if (sourceResourceFormat == ResourceFormat.TSV) {
            return () -> {
                LOG.debug("Using \\t as default value of delimiter to be compliant TSV standard.");
                return '\t';
            };
        }
        return () -> {
            LOG.debug("Using coma as default value of delimiter.");
            return ',';
        };
    }

    private Supplier<Character> getDefaultQuoteCharacterSupplier(ResourceFormat sourceResourceFormat) {
        if (sourceResourceFormat == ResourceFormat.CSV) {
            return () -> {
                LOG.debug("Quote character not specified, using double-quote as default value" +
                        " to be compliant with RFC 4180 (CSV)");
                return '"';
            };
        }
        return () -> '\0';
    }

    private char getPropertyValue(Property property,
                                  Supplier<Character> defaultValueSupplier) {
        return Optional.ofNullable(getPropertyValue(property))
                .map(n -> n.asLiteral().getChar())
                .orElseGet(defaultValueSupplier);
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
        if (outputMode == Mode.STANDARD) {
            // 2
            tableGroup = new TableGroup();
            if (tableGroupUri != null && !tableGroupUri.isEmpty()) {
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
        if ((sourceResourceFormat == ResourceFormat.CSV && delimiter != ',') ||
                (sourceResourceFormat == ResourceFormat.TSV && delimiter != '\t')) {
            throw new SpecificationNonComplianceException(sourceResourceFormat, delimiter);
        }
        this.delimiter = delimiter;
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

    public void setSourceResourceFormat(ResourceFormat sourceResourceFormat) {
        this.sourceResourceFormat = sourceResourceFormat;
    }

    public void processTableAtIndex(int sheetNumber) {
        this.processTableAtIndex = sheetNumber;
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

            } else {
                LOG.info("Order of columns was not provided in the schema.");
            }
        } else {
            header = createHeaders(header.length, new ArrayList<>());
        }
        return header;
    }

    private String[] createHeaders(int size, List<Column> columns) {
        String[] headers = new String[size];

        for (int i = 0; i < size; i++) {
            if (!columns.isEmpty()) {
                headers[i] = columns.get(i).getName();
            } else {
                headers[i] = "column_" + (i + 1);
            }
        }
        return headers;
    }
}
