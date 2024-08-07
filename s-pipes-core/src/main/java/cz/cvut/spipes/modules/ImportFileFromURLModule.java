package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportFileFromURLModule extends AnnotatedAbstractModule {

    private static final Logger log = LoggerFactory.getLogger(ImportFileFromURLModule.class);

    //sml:targetFilePath, required

    @Parameter(urlPrefix = SML.uri, name = "targetFilePath", comment = "")
    Path targetFilePath; //TODO $_executionDir ?

    //kbss:targetResourceVariable
    String targetResourceVariable;

    //sml:url, required
    @Parameter(urlPrefix = SML.uri, name = "url", comment = "")
    URL url;

    @Override
    public ExecutionContext executeSelf() {

        Path computedTargetFilePath = null;

        log.debug("Importing file from url {}.", url);
        try (InputStream inputStream = url.openStream()) {

            computedTargetFilePath =
                    Optional.ofNullable(targetFilePath)
                            .orElse(Files.createTempFile("", ".tmp"));

            Files.copy(inputStream, computedTargetFilePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            log.error("Could not download data from url {}.", url);
            throw new RuntimeException(e);
        }

        if ((targetResourceVariable == null) || (targetResourceVariable.equals(""))) {
            return ExecutionContextFactory.createEmptyContext();
        }

        return ExecutionContextFactory.createContext(
                new VariablesBinding(targetResourceVariable,
                        ResourceFactory.createStringLiteral(computedTargetFilePath.toString()))
        );
    }

    @Override
    public String getTypeURI() {
        return SML.ImportFileFromURL.getURI();
    }


    public Path getTargetFilePath() {
        return targetFilePath;
    }

    public void setTargetFilePath(Path targetFilePath) {
        this.targetFilePath = targetFilePath;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getTargetResourceVariable() {
        return targetResourceVariable;
    }

    public void setTargetResourceVariable(String targetResourceVariable) {
        this.targetResourceVariable = targetResourceVariable;
    }
}
