package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.util.ExecUtils;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

public class ExternalSchemExModule extends AbstractModule {

    private static final String MODULE_ID = "external-schemex";
    private static final String TYPE_URI = KBSS_MODULE.uri + MODULE_ID;
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final String SCHEMEX_PROGRAM = "schemex";
    //sml:sourceFilePath
    private Path sourceFilePath;

    @Override
    ExecutionContext executeSelf() {

        File tempFile = ExecUtils.createTempFile();

        // execute tarql query.sparql table.csv
        String[] programCall = new String[] {
            SCHEMEX_PROGRAM,
            tempFile.getAbsolutePath(),
            sourceFilePath.toAbsolutePath().toString()
        };


        InputStream is = ExecUtils.execProgramWithoutExeption(programCall, null);

        return ExecutionContextFactory.createContext(
            ModelFactory.createDefaultModel().read(is, null, FileUtils.langTurtle)
        );
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {

        sourceFilePath = Paths.get(getEffectiveValue(SML.sourceFilePath).asLiteral().toString());

    }
}