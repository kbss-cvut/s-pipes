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

    // private String delimiter;

    //sml:replace
    private boolean isReplace;

    //sml:sourceFilePath
    private String sourceFilePath;

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

    //TODO konfigurace
    public final String dataPrefix = "http://onto.fel.cvut.cz/data/";

    @Override
    ExecutionContext executeSelf() {
        Model outputModel = ModelFactory.createDefaultModel();

        // TODO configure delimiter
        CsvPreference csvPreference = new CsvPreference.Builder('"',
            '\t',
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
            // TODO log
        } finally {
            if( listReader != null ) {
//                listReader.close();
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
