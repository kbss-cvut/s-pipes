package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.exception.ResourceNotFoundException;
import cz.cvut.spipes.modules.tabular.Mode;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StreamResourceRegistry;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
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
import java.util.List;

/**
 * Module for converting tabular data (e.g. CSV or TSV) to RDF
 * <p>
 * The implementation loosely follows the W3C Recommendation described here:
 * <a href="https://www.w3.org/TR/csv2rdf/">Generating RDF from Tabular Data on the Web</a>
 * <p>
 * <b>Important notes:</b><br/>
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
    public String dataPrefix; // dataprefix#{_column}

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

    @Override
    ExecutionContext executeSelf() {
        outputModel = ModelFactory.createDefaultModel();
        //this.executionContext.getDefaultModel();

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

            for (String columnName : header) {
                Resource columnResource = ResourceFactory.createResource();

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
                        CSVW.aboutUrl,
                        outputModel.createTypedLiteral(sourceResource.getUri() + "/columns/" + columnName + "-{_row}", CSVW.uriTemplate)
                );
            }

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
                    final String rowIri = T.getURI() + "#row=" + listReader.getRowNumber();
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

                // 4.6.8
                // Establish a new blank node Sdef to be used as the default subject for cells where about URL is undefined
                Resource S_def = ResourceFactory.createResource();

                for (int i = 0; i < header.length; i++) {
                    // 4.6.8.1
                    String aboutUrl = null; //TODO get from user

                    Resource S;
                    if (aboutUrl != null && !aboutUrl.isEmpty()) {
                        S = ResourceFactory.createResource(aboutUrl);
                    } else {
                        S = S_def;
                    }

                    // 4.6.8.2
                    if (R != null) {
                        outputModel.add(ResourceFactory.createStatement(
                                R,
                                CSVW.describes,
                                S));
                    }

                    // 4.6.8.3
                    String propertyUrl = null; //TODO get from user
                    String columnName = header[i];

                    Property P;
                    if (propertyUrl != null && !propertyUrl.isEmpty()) {
                        P = ResourceFactory.createProperty(propertyUrl);
                    } else if (dataPrefix != null && !dataPrefix.isEmpty()) {
                        P = ResourceFactory.createProperty(
                                dataPrefix + URLEncoder.encode(columnName, "UTF-8"));
                    } else {
                        P = ResourceFactory.createProperty(
                                sourceResource.getUri() + "#" + URLEncoder.encode(columnName, "UTF-8")); //TODO should be URL (according to specification) not URI
                    }

                    String valueUrl = null; //TODO get from user

                    if (valueUrl != null && !valueUrl.isEmpty()) {
                        // 4.6.8.4
                        Resource V_url = ResourceFactory.createResource(valueUrl);
                        outputModel.add(ResourceFactory.createStatement(
                                S,
                                P,
                                V_url));
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

        return ExecutionContextFactory.createContext(outputModel);
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
            outputModel.add(
                    T_Schema,
                    CSVW.aboutUrl,
                    outputModel.createTypedLiteral(sourceResource.getUri() + "#table---{_table}", CSVW.uriTemplate)
            );
        }
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

    private Resource getResource(String name) {
        return ResourceFactory.createResource(dataPrefix + name);
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
}
