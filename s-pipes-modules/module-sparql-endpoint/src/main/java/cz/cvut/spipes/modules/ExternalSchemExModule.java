package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.util.ExecUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

@SPipesModule(label = "external schemex", comment = "external schemex")
public class ExternalSchemExModule extends AbstractModule {

    private static final String MODULE_ID = "external-schemex";
    private static final String TYPE_URI = KBSS_MODULE.uri + MODULE_ID;
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final String SCHEMEX_PROGRAM = "schemex";
    //sml:sourceFilePath
    @Parameter(urlPrefix = SML.uri, name = "sourceFilePath")
    private Path sourceFilePath;

    @Override
    ExecutionContext executeSelf() {

        Path outputDir = getOutputDirectory();

        String[] programCall = new String[] {
            SCHEMEX_PROGRAM,
            "-f",
            sourceFilePath.toString(),
            "-o",
            outputDir.toString()
        };

        ExecUtils.execProgramWithoutExeption(programCall, null);

        InputStream is = null;
        return ExecutionContextFactory.createContext(
            ModelFactory.createDefaultModel().read(is, null, FileUtils.langTurtle)
        );
    }

    private Path getOutputDirectory() {
        Path outputDir = null;
        try {
            outputDir = Files.createTempFile("schemex-", "");

        } catch (IOException e) {
            new RuntimeException("Could not create temporary directory " + outputDir + ".", e);
        }
        return outputDir;
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