package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.util.ExecUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SPipesModule(label = "external schemex", comment = "Compute schemex using external script for a specified sourceFilePath.")
public class ExternalSchemExModule extends AnnotatedAbstractModule {

    private static final String MODULE_ID = "external-schemex";
    private static final String TYPE_URI = KBSS_MODULE.uri + MODULE_ID;
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final String SCHEMEX_PROGRAM = "schemex";

    @Parameter(iri = SML.sourceFilePath, comment = "Source file in nt format.")
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

}