package cz.cvut.spipes.modules;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.spipes.config.ExecutionConfig;
import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.exception.ResourceNotFoundException;
import cz.cvut.spipes.exception.ResourceNotUniqueException;
import cz.cvut.spipes.modules.exception.TableSchemaException;
import cz.cvut.spipes.modules.model.Column;
import cz.cvut.spipes.modules.model.TableSchema;
import cz.cvut.spipes.modules.util.JopaPersistenceUtils;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StreamResourceRegistry;
import cz.cvut.spipes.util.JenaUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.iri.IRI;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.shared.PropertyNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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

    //sml:replace
    private boolean isReplace;

    //:source-resource-uri
    private StreamResource sourceResource;

    //:delimiter
    private int delimiter;

    //:quote-character
    private char quoteCharacter;

    //:data-prefix
    public String dataPrefix;

    //:output-mode
    private Mode outputMode;

    /**
     * Represent a root resource for group of tables.
     */
    private Resource G;

    /**
     * Represent a root resource for a table.
     */
    private Resource T;

    /**
     * Represents the resource for the table schema that was used to describe the table
     */
    private Resource T_Schema;

    private Model outputModel;

    private TableSchema inputTableSchema = new TableSchema();

    private boolean hasTableSchema = false;

    @Override
    ExecutionContext executeSelf() {
        Model inputModel = executionContext.getDefaultModel();

        outputModel = ModelFactory.createDefaultModel();
        EntityManager em = JopaPersistenceUtils.getEntityManager("cz.cvut.spipes.modules.model", inputModel);
        em.getTransaction().begin();

        onTableGroup(null);
        onTable(null);

        CsvPreference csvPreference = new CsvPreference.Builder(
            quoteCharacter,
            delimiter,
            "\\n").build();

        ICsvListReader listReader = null;
        try {
            listReader = new CsvListReader(getReader(), csvPreference);

            String[] header = listReader.getHeader(true); // skip the header (can't be used with CsvListReader)

            Set<String> columnNames = new HashSet<>();
            List<RDFNode> columns = new LinkedList<>();

            try {
                inputTableSchema = em.createNativeQuery(
                        "PREFIX csvw: <http://www.w3.org/ns/csvw#>\n" +
                                "SELECT ?t WHERE { \n" +
                                "?t a csvw:TableSchema. \n" +
                                "}",
                        TableSchema.class
                ).getSingleResult();
                hasTableSchema = true;
                LOG.debug("Custom table schema found.");
            } catch (NoResultException e) {
                LOG.debug("No custom table schema found.");
            }

            String mainErrorMsg = "CSV table schema is not compliant with provided custom schema.";

            if (hasTableSchema && header.length != inputTableSchema.getColumnsSet().size()) {

                String mergedMsg = mainErrorMsg + "\n" +
                        "The number of columns in the table schema does not match the number of columns in the table." + "\n"
//                        .append(evidence).append("\n") TODO: rdf triples of evidence
                        ;

                if (ExecutionConfig.isExitOnError()) {
                    throw new TableSchemaException(mergedMsg, this);
                }else LOG.error(mergedMsg);
            }

            List<Column> schemaColumns = new ArrayList<>(header.length);

            int j = 0;
            for (String columnTitle : header) {
                Resource columnResource = ResourceFactory.createResource();
                String columnName = normalize(columnTitle);
                boolean isDuplicate = !columnNames.add(columnName);
                columns.add(columnResource);

                if (hasTableSchema){
                    Column schemaColumn = getColumnFromTableSchema(columnTitle, inputTableSchema);
                    schemaColumns.add(schemaColumn);
                    if (schemaColumn == null) {
                        String mergedMsg = mainErrorMsg + "\n" +
                                "Column with name '" + columnTitle + "' is missing." + "\n"
//                        .append(evidence).append("\n") TODO: rdf triples of evidence
                                ;

                        if (ExecutionConfig.isExitOnError()) {
                            throw new TableSchemaException(mergedMsg, this);
                        }else LOG.error(mergedMsg);
                    }
                }


                if (isDuplicate) {
                    Resource collidingColumn = getColumnByName(columnName);
                    throw new ResourceNotUniqueException(
                            String.format("Unable to create value of property %s due to collision. " +
                                    "Both column titles '%s' and '%s' are normalized to '%s' " +
                                    "and thus would refer to the same property url <%s>.",
                                    CSVW.propertyUrl,
                                    columnTitle,
                                    collidingColumn.getRequiredProperty(CSVW.title).getObject().asLiteral().toString(),
                                    columnName,
                                    collidingColumn.getRequiredProperty(CSVW.propertyUrl).getObject().asLiteral().toString()));
                }

                outputModel.add(
                        T_Schema,
                        CSVW.column,
                        columnResource
                );
                outputModel.add(
                        columnResource,
                        CSVW.name,
                        ResourceFactory.createStringLiteral(columnName)
                );
                outputModel.add(
                    columnResource,
                    CSVW.title,
                    ResourceFactory.createStringLiteral(columnTitle)
                );

                String columnAboutUrl = null;
                if(hasTableSchema) columnAboutUrl = schemaColumns.get(j).getAboutUrl();

                if (columnAboutUrl != null && !columnAboutUrl.isEmpty()) {
                    outputModel.add(
                            columnResource,
                            CSVW.aboutUrl,
                            outputModel.createTypedLiteral(columnAboutUrl, CSVW.uriTemplate)
                    );
                } else {
                    outputModel.add(
                            T_Schema,
                            CSVW.aboutUrl,
                            outputModel.createTypedLiteral(sourceResource.getUri() + "#row-{_row}", CSVW.uriTemplate)
                    );
                }


                String columnPropertyUrl = null;
                if (columnPropertyUrl != null && !columnPropertyUrl.isEmpty()) {
                    outputModel.add(
                            columnResource,
                            CSVW.propertyUrl,
                            outputModel.createTypedLiteral(columnPropertyUrl, CSVW.uriTemplate)
                    );
                } else if (dataPrefix != null && !dataPrefix.isEmpty()) {
                    outputModel.add(
                            columnResource,
                            CSVW.propertyUrl,
                            ResourceFactory.createPlainLiteral(dataPrefix + URLEncoder.encode(columnName, "UTF-8")) //TODO should be URL (according to specification) not URI
                    );
                } else {
                    outputModel.add(
                            columnResource,
                            CSVW.propertyUrl,
                            ResourceFactory.createPlainLiteral(sourceResource.getUri() + "#" + URLEncoder.encode(columnName, "UTF-8"))
                    );
                }

                j++;
            }

            RDFList columnList = outputModel.createList(columns.iterator());

            outputModel.add(
                    T_Schema,
                    CSVW.columns,
                    columnList);

            List<String> row;
            int rowNumber = 0;
            //for each row
            while( (row = listReader.read()) != null ) {
                rowNumber++;

                // 4.6
                Resource R;
                if (outputMode == Mode.STANDARD) {
                    // 4.6.1
                    R = ResourceFactory.createResource();
                    // 4.6.2
                    outputModel.add(
                            T,
                            CSVW.row,
                            R);
                    // 4.6.3
                    outputModel.add(
                            R,
                            RDF.type,
                            CSVW.Row);
                    // 4.6.4
                    outputModel.add(
                            R,
                            CSVW.rowNum,
                            ResourceFactory.createTypedLiteral(Integer.toString(rowNumber),
                                    XSDDatatype.XSDinteger));
                    // 4.6.5
                    final String rowIri = sourceResource.getUri() + "#row=" + listReader.getRowNumber();
                    outputModel.add(
                            R,
                            CSVW.url,
                            ResourceFactory.createResource(rowIri));
                    // 4.6.6 - Add titles.
                    // We do not support titles.

                    // 4.6.7
                    // In standard mode only, emit the triples generated by running
                    // the algorithm specified in section 6. JSON-LD to RDF over any
                    // non-core annotations specified for the row, with node R as
                    // an initial subject, the non-core annotation as property, and the
                    // value of the non-core annotation as value.
                } else {
                    R = null;
                }

                for (int i = 0; i < header.length; i++) {
                    Resource schemaColumnResource = columns.get(i).asResource();

                    // 4.6.8.1
                    String columnAboutUrlStr = getAboutUrlFromSchema(schemaColumnResource);

                    //TODO: Is this neccesary?
//                    String columnAboutUrlStr = getAboutUrlFromSchema(schemaColumnResource);
//                    UriTemplate aboutUrlTemplate = new UriTemplate(columnAboutUrlStr);
//                    aboutUrlTemplate.initialize(null, Arrays.asList(header));
//                    IRI columnAboutUrl = aboutUrlTemplate.getUri(row);

                    Resource S = ResourceFactory.createResource(columnAboutUrlStr.replace(
                            "{_row}",
                            Integer.toString(listReader.getRowNumber())
                    ));

                    // 4.6.8.2
                    if (R != null) {
                        outputModel.add(ResourceFactory.createStatement(
                                R,
                                CSVW.describes,
                                S));
                    }

                    // 4.6.8.3
                    String columnPropertyUrl = getPropertyUrlFromSchema(schemaColumnResource);
                    Property P = ResourceFactory.createProperty(columnPropertyUrl);

                    Column column = getColumnFromTableSchema(header[i], inputTableSchema);
                    String valueUrl = null;
                    if(column != null) valueUrl = column.getValueUrl();

                    if (valueUrl != null && !valueUrl.isEmpty()) {
                        // 4.6.8.4
                        Resource V_url = ResourceFactory.createResource(valueUrl);
                        outputModel.add(ResourceFactory.createStatement(
                                S,
                                P,
                                V_url));

                        outputModel.add(
                                schemaColumnResource,
                                CSVW.valueUrl,
                                ResourceFactory.createPlainLiteral(valueUrl)
                        );
                    } else {
                        final String cellValue = row.get(i);
                        if (cellValue != null) {
                            outputModel.add(ResourceFactory.createStatement(
                                S,
                                P,
                                ResourceFactory.createPlainLiteral(cellValue)));
                        }
                    }

                    // 4.6.8.5 - else, if value is list and cellOrdering == true
                    // 4.6.8.6 - else, if value is list
                    // 4.6.8.7 - else, if cellValue is not null
                }
            }

        } catch (IOException e) {
            LOG.error("Error while reading file from resource uri {}", sourceResource, e);
        } finally {
            if( listReader != null ) {
                try {
                    listReader.close();
                } catch (IOException e) {
                    LOG.error("Error while closing file from resource uri {}", sourceResource, e);
                }
            }
        }

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

    private void onTableGroup(String tableGroupUri) {
        if (outputMode == Mode.STANDARD) {
            // 1
            if (tableGroupUri != null && !tableGroupUri.isEmpty()) {
                G = ResourceFactory.createResource(tableGroupUri);
            } else {
                G = ResourceFactory.createResource();
            }
            // 2
            outputModel.add(
                    G,
                    RDF.type,
                    CSVW.TableGroup);
            // 3
            // In standard mode only, emit the triples generated by running
            // the algorithm specified in section 6. JSON-LD to RDF over any
            // notes and non-core annotations specified for the group of tables,
            // with node G as an initial subject, the notes or non-core
            // annotation as property, and the value
            // of the notes or non-core annotation as value.
        }
    }

    private void onTable(String tableUri) {
        // 4 - we never skip table
        if (outputMode == Mode.STANDARD) {
            // 4.1 (establish a new node T which represents the current table)
            if (tableUri != null && !tableUri.isEmpty()) {
                T = ResourceFactory.createResource(tableUri);
            } else {
                T = ResourceFactory.createResource();
            }
            // 4.2
            outputModel.add(
                    G,
                    CSVW.table,
                    T);
            // 4.3
            outputModel.add(
                    T,
                    RDF.type,
                    CSVW.Table);
            // 4.4 (specify the source tabular data file URL for the current table based on the url annotation)
            outputModel.add(
                    T,
                    CSVW.url,
                    ResourceFactory.createResource(sourceResource.getUri())); //TODO should be URL (according to specification) not URI

            // 4.5
            // In standard mode only, emit the triples generated by running
            // the algorithm specified in section 6. JSON-LD to RDF over any
            // notes and non-core annotations specified for the table, with
            // node T as an initial subject, the notes or non-core
            // annotation as property, and the value
            // of the notes or non-core annotation as value.

            T_Schema = ResourceFactory.createResource();
            outputModel.add(
                    T,
                    CSVW.tableSchema,
                    T_Schema
            );
            outputModel.add(
                    T_Schema,
                    RDF.type,
                    CSVW.TableSchema
            );
        }
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

    private String getAboutUrlFromSchema(Resource columnResource) {
        String aboutUrl;
        try {
            aboutUrl = outputModel.getRequiredProperty(columnResource, CSVW.aboutUrl).getObject().asLiteral().getString();
        } catch (PropertyNotFoundException e) {
            aboutUrl = outputModel.getRequiredProperty(T_Schema, CSVW.aboutUrl).getObject().asLiteral().getString();
        }
        return aboutUrl;
    }

    private String getPropertyUrlFromSchema(Resource columnResource) {
        return outputModel.getRequiredProperty(columnResource, CSVW.propertyUrl).getObject().asLiteral().getString();
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

    private Column getColumnFromTableSchema(String columnTitle, TableSchema tableSchema) {
        for (Column column : tableSchema.getColumnsSet()) {
            if (column.getTitle() != null && column.getTitle().equals(columnTitle)) {
                return column;
            }
        }
        return null;
    }
}
