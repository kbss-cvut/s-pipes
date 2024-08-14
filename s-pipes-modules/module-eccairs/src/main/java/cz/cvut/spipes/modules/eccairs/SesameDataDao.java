package cz.cvut.spipes.modules.eccairs;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.ByteArrayOutputStream;
import java.net.URI;

@Slf4j
public class SesameDataDao {

    /**
     * Gets raw content of the repository. Use null for contextUri to get whole content of the repository.
     * <p>
     * The data are serialized using Sesame's {@link RDFXMLPrettyWriter}.
     *
     * @return Repository content serialized as String
     */
    public static String getRepositoryData(Repository sesameRepository, URI contextUri) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final RDFHandler rdfHandler = new RDFXMLPrettyWriter(bos);
        getRepositoryData(sesameRepository, contextUri, rdfHandler);
        return new String(bos.toByteArray());
    }

    /**
     * Exports repository data from the specified context and passes it to the handler.
     *
     * @param contextUri Context from which the data should be exported. Optional
     * @param handler    Handler for the exported data
     */
    private static void getRepositoryData(Repository sesameRepository, URI contextUri, RDFHandler handler) {
        try {
            final RepositoryConnection connection = sesameRepository.getConnection();
            try {
                final ValueFactory valueFactory = connection.getValueFactory();
                if (contextUri != null) {
                    connection.export(handler, valueFactory.createIRI(contextUri.toString()));
                } else {
                    connection.export(handler);
                }
            } finally {
                connection.close();
            }
        } catch (RepositoryException | RDFHandlerException e) {
            log.error("Unable to read data from repository.", e);
        }
    }
}
