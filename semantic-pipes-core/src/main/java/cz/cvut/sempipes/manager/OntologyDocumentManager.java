package cz.cvut.sempipes.manager;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;

/**
 * Keeps m
 * <p>
 * <p>
 * Created by Miroslav Blasko on 22.7.16.
 */
public interface OntologyDocumentManager {

    /**
     * Returns list of all supported file extension that this document manager can load.
     *
     * @return
     */
    List<String> getSupportedFileExtensions();

    /**
     * Registers ontology documents from a file system path.
     * If directory is provided, it is recursively crawled for files with supported file extensions.
     *
     * @param fileOrDirectoryPath
     */
    void registerDocuments(Path fileOrDirectoryPath);


    Set<String> getRegisteredOntologyUris();

    OntModel getOntology(String uri);

    Model getModel(String uri);

    OntDocumentManager getOntDocumentManager();

    void reset();
}
