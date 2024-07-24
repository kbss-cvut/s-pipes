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

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class ImportFileFromURLModule extends AbstractModule {

    //sml:targetFilePath, required
    Path targetFilePath; //TODO $_executionDir ?

    //kbss:targetResourceVariable
    String targetResourceVariable;

    //sml:url, required
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

    @Override
    public void loadConfiguration() {

        RDFNode urlNode = getEffectiveValue(SML.url);
        try {
            url = new URL(urlNode.asLiteral().toString());
        } catch (MalformedURLException e) {
            log.error("Malformed url -- {}.", getEffectiveValue(SML.url));
            throw new RuntimeException(e);
        }

        targetFilePath = Paths.get(getEffectiveValue(SML.targetFilePath).asLiteral().toString());
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
