package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class TabularToRDFModelModule extends AbstractModule {

    public static final String TYPE_URI = KBSS_MODULE.uri + "tabular";
    private static final Logger LOG = LoggerFactory.getLogger(TabularToRDFModelModule.class);

    //sml:replace
    private boolean isReplace;

    //sml:sourceFilePath
    private String sourceFilePath;

    //sml:delimiter
    private int delimiter;

    //sml:dataPrefix
    public String dataPrefix;

    public boolean isReplace() {
        return isReplace;
    }

    public void setReplace(boolean replace) {
        isReplace = replace;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    public int getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(int delimiter) {
        this.delimiter = delimiter;
    }

    public String getDataPrefix() {
        return dataPrefix;
    }

    public void setDataPrefix(String dataPrefix) {
        this.dataPrefix = dataPrefix;
    }

    @Override
    ExecutionContext executeSelf() {
        Model outputModel = ModelFactory.createDefaultModel();

        CsvPreference csvPreference = new CsvPreference.Builder('"',
            delimiter,
            "\\n").build();

        // for each row
        ICsvListReader listReader = null;
        try {
            listReader = new CsvListReader(getFileReader(), csvPreference);

            String[] header = listReader.getHeader(true); // skip the header (can't be used with CsvListReader)

            List<String> row;
            while( (row = listReader.read()) != null ) {

                Resource rowResource = outputModel.createResource(dataPrefix + listReader.getRowNumber());

                for (int i = 0; i < header.length; i++) {
                    outputModel.add(getCellStatement(rowResource, header[i], row.get(i)));
                }
            }

        } catch (IOException e) {
            LOG.error("Error while reading file {}", sourceFilePath, e);
        } finally {
            if( listReader != null ) {
                try {
                    listReader.close();
                } catch (IOException e) {
                    LOG.error("Error while closing file {}", sourceFilePath, e);
                }
            }
        }

        return ExecutionContextFactory.createContext(outputModel);
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {
        isReplace = this.getPropertyValue(SML.replace, false);
        sourceFilePath = getEffectiveValue(SML.sourceFilePath).asLiteral().toString();
        delimiter = getPropertyValue(SML.delimiter, '\t');
        dataPrefix = getEffectiveValue(SML.dataPrefix).asLiteral().toString();
    }

    private FileReader getFileReader() throws FileNotFoundException {
        return new FileReader(sourceFilePath);
    }

    private Statement getCellStatement(Resource rowResource, String columnName, String value) {
        return rowResource.getModel().createStatement(
            rowResource,
            ResourceFactory.createProperty(dataPrefix, columnName),
            ResourceFactory.createPlainLiteral(value)
        );
    }

    private Resource getResource(String name) {
        return ResourceFactory.createResource(dataPrefix + name);
    }
}
