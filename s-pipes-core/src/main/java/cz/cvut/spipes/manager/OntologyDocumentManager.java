package cz.cvut.spipes.manager;

import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

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
     * Supported file extensions are returned by <code>getSupportedFileExtensions()</code>.
     *
     * @param fileOrDirectoryPath File or directory path to register. If directory, it is recursively crawled.
     */
    void registerDocuments(Path fileOrDirectoryPath);
    void registerDocuments(Iterable<Path> fileOrDirectoryPath);


    Set<String> getRegisteredOntologyUris();

    OntModel getOntology(String uri);

    Model getModel(String uri);

    OntDocumentManager getOntDocumentManager();

    void reset();
}
