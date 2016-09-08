package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SM;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Miroslav Blasko on 28.5.16.
 */
public class ImportFileFromURLModule extends AbstractModule  {

    private static final Logger LOG = LoggerFactory.getLogger(ImportFileFromURLModule.class);

    //sml:targetFilePath, required
    Path targetFilePath; //TODO pojde $_executionDir ?  String targetResourceVariable;

    //sml:url, required
    URL url;

    @Override
    public ExecutionContext executeSelf() {

        String s = null;
        try {
            s = IOUtils.toString(url, Charset.defaultCharset());
        } catch (IOException e) {
            LOG.error("Could not download from url {}.", url);
            throw new RuntimeException(e);
        }
        return null;
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
}
