package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.exception.ContextNotFoundException;
import cz.cvut.spipes.manager.OntoDocManager;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportRDFFromWorkspaceModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ImportRDFFromWorkspaceModule.class);

    //TODO refactor -> should be part of execution context
    OntologyDocumentManager ontologyDocumentManager = OntoDocManager.getInstance();

    // sml:baseURI : xsd:string
    String baseUri;

    // sml:ignoreImports : xsd:boolean
    boolean isIgnoreImports;

    // TODO reconsider support for this property (might change identification of module type)
    // sml:sourceFilePath : xsd:string
    Path sourceFilePath;


    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public boolean isIgnoreImports() {
        return isIgnoreImports;
    }

    public void setIgnoreImports(boolean ignoreImports) {
        isIgnoreImports = ignoreImports;
    }

    public Path getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(Path sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    @Override
    public String getTypeURI() {
        return SML.ImportRDFFromWorkspace.toString();
    }

    @Override
    public void loadConfiguration() {
        baseUri = getStringPropertyValue(SML.baseURI);
        isIgnoreImports = getPropertyValue(SML.ignoreImports, false);
        sourceFilePath = Optional.ofNullable(getEffectiveValue(SML.sourceFilePath))
                        .filter(RDFNode::isLiteral)
                        .map(RDFNode::asLiteral)
                        .map(Object::toString)
                        .map(s -> Paths.get(s))
                        .orElse(null);

    }

    @Override
    ExecutionContext executeSelf() {

        if (! isIgnoreImports) {
            throw new IllegalArgumentException("Module property " + SML.ignoreImports + " with value \"false\" is not implemented." );
        }
        if (sourceFilePath != null) {
            throw new IllegalArgumentException("Module property " + SML.sourceFilePath + " is not implemented." );
        }

        Model workspaceModel = ontologyDocumentManager.getOntology(baseUri);

        if (workspaceModel == null) {
            throw new ContextNotFoundException(baseUri);
        }

        return ExecutionContextFactory.createContext(workspaceModel);

    }

    void setOntologyDocumentManager(OntologyDocumentManager ontologyDocumentManager) {
        this.ontologyDocumentManager = ontologyDocumentManager;
    }
}
