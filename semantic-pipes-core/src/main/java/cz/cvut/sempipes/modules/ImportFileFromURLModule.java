package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SM;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import cz.cvut.sempipes.registry.ResourceRegistry;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Optional;

/**
 * Created by Miroslav Blasko on 28.5.16.
 */
public class ImportFileFromURLModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ImportFileFromURLModule.class);

    //sml:targetFilePath, required
    Path targetFilePath; //TODO $_executionDir ?

    //kbss:targetResourceVariable
    String targetResourceVariable;

    //sml:url, required
    URL url;

    @Override
    public ExecutionContext executeSelf() {

        Path computedTargetFilePath = null;

        LOG.debug("Importing file from url {}.", url);
        try (InputStream inputStream = url.openStream()) {

            computedTargetFilePath =
                    Optional.ofNullable(targetFilePath)
                            .orElse(Files.createTempFile("", ".tmp"));

            Files.copy(inputStream, computedTargetFilePath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            LOG.error("Could not download data from url {}.", url);
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
            LOG.error("Malformed url -- {}.", getEffectiveValue(SML.url));
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
