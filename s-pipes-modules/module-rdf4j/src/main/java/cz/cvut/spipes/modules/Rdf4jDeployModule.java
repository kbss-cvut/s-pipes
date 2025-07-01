package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.exception.ModuleConfigurationInconsistentException;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.util.CoreConfigProperies;
import cz.cvut.spipes.util.JenaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.jetbrains.annotations.NotNull;

import jakarta.annotation.Nullable;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@SPipesModule(label = "deploy", comment =
        "Module deploys content of input execution context into default context of repository (if p-rdf4j-context-iri " +
        "is not specified) or concrete context (if p-rdf4j-context-iri is specified)."
)
public class Rdf4jDeployModule extends AnnotatedAbstractModule {

    private final static String TYPE_URI = KBSS_MODULE.uri + "deploy";
    private final static String PROPERTY_PREFIX_URI = KBSS_MODULE.uri + "rdf4j";

    @Parameter(iri = PROPERTY_PREFIX_URI + "/" + "p-rdf4j-server-url", comment = "URL of the Rdf4j server")
    private String rdf4jServerURL;

    @Parameter(iri = PROPERTY_PREFIX_URI + "/" + "p-rdf4j-repository-name", comment = "Rdf4j repository ID")
    private String rdf4jRepositoryName;

    @Parameter(iri = PROPERTY_PREFIX_URI + "/" + "p-rdf4j-context-iri", comment = "IRI of the context that should be used for deployment.")
    private String rdf4jContextIRI;

    @Parameter(iri = PROPERTY_PREFIX_URI + "/" + "p-rdf4j-infer-context-iris",
        comment = "IRI of contexts is inferred from annotated input triples. Only reified triples that contain triple " +
            "?reifiedStatement kbss-module:is-part-of-graph ?graph are processed." +
            " Actual triples related to reified statement are not processed/needed. Default is false.")
    private boolean inferContextIRIs = false;

    @Parameter(iri = PROPERTY_PREFIX_URI + "/" + "p-rdf4j-secured-username-variable", comment = "User name if the repository requires authentication.")
    private String rdf4jSecuredUsernameVariable;

    private RepositoryManager repositoryManager;

    private Repository repository;

    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    @Parameter(iri = PROPERTY_PREFIX_URI + "/" + "p-rdf4j-secured-password-variable", comment = "Password if the repository requires authentication.")
    private String rdf4jSecuredPasswordVariable;

    @Parameter(iri = PROPERTY_PREFIX_URI + "/" + "p-is-replace", comment =
            "Whether data should be replaced (true) / appended (false) into the specified context or repository.\n" +
            "Default is false.")
    private boolean isReplaceContext = false;

    public String getRdf4jServerURL() {
        return rdf4jServerURL;
    }

    public void setRdf4jServerURL(String rdf4jServerURL) {
        this.rdf4jServerURL = rdf4jServerURL;
    }

    public String getRdf4jRepositoryName() {
        return rdf4jRepositoryName;
    }

    public void setRdf4jRepositoryName(String rdf4jRepositoryName) {
        this.rdf4jRepositoryName = rdf4jRepositoryName;
    }

    public String getRdf4jContextIRI() {
        return rdf4jContextIRI;
    }

    public void setRdf4jContextIRI(String rdf4jContextIRI) {
        this.rdf4jContextIRI = rdf4jContextIRI;
    }

    public boolean isReplaceContext() {
        return isReplaceContext;
    }

    public void setReplaceContext(boolean replaceContext) {
        isReplaceContext = replaceContext;
    }

    @Override
    ExecutionContext executeSelf() {
        log.debug("Deploying data into {} of rdf4j server repository {}/repositories/{}.",
            getContextsInfo(),
            rdf4jServerURL,
            rdf4jRepositoryName);
        RepositoryConnection connection = null;
        try {

            if (repository == null) {
                log.info("Creating new repository {} within rdf4j server {} ...",
                    rdf4jServerURL, rdf4jRepositoryName);
                RepositoryConfig repConfig = new RepositoryConfig(rdf4jRepositoryName);
                SailRepositoryConfig config = new SailRepositoryConfig(new NativeStoreConfig());
                repConfig.setRepositoryImplConfig(config);
                repositoryManager.addRepositoryConfig(repConfig);
                repository = repositoryManager.getRepository(rdf4jRepositoryName);
            }

            Dataset dataset = createDataset(executionContext.getDefaultModel(), rdf4jContextIRI, inferContextIRIs);

            if (dataset.isEmpty()) {
                log.info("No triples found to deploy.");
                return ExecutionContextFactory.createContext(executionContext.getDefaultModel());
            }

            repository.init();
            connection = repository.getConnection();

            connection.begin();
            if (isReplaceContext) {
                ArrayList<Resource> contextList = new ArrayList<>();
                RepositoryConnection finalConnection = connection;
                dataset.listNames().forEachRemaining(
                    c -> contextList.add(finalConnection.getValueFactory().createIRI(c))
                );
                connection.clear(contextList.toArray(new Resource[0]));
            }

            StringWriter w = new StringWriter();
            RDFDataMgr.write(w, dataset, RDFLanguages.NQUADS);

            connection.add(new StringReader(w.getBuffer().toString()), "", RDFFormat.NQUADS);
            connection.commit();
        } catch (final RepositoryException | RDFParseException | RepositoryConfigException | IOException e) {
            log.error(e.getMessage(),e);
        } finally {
            try {
                if (connection != null && connection.isOpen()) {
                    connection.close();
                }
            } catch (RepositoryException e) {
                log.error(e.getMessage(), e);
            } finally {
                if ((repository != null) && (repository.isInitialized())) {
                    try {
                        repository.shutDown();
                    } catch (RepositoryException e) {
                        log.error("During finally: ", e);
                    }
                }
            }
        }

        return ExecutionContextFactory.createContext(executionContext.getDefaultModel());
    }

    private @NotNull String getContextsInfo() {
        if (isRdf4jContextIRIDefined()) {
            return "context " + rdf4jContextIRI;
        } else if (inferContextIRIs) {
            return "inferred contexts";
        } else {
            return "default context";
        }
    }

    static Dataset createDataset(@NotNull Model model, String outputGraphId, boolean inferGraphsFromAnnotatedModel) {
        boolean isOutputGraphSpecified = (outputGraphId != null) && (!outputGraphId.isEmpty());
        if (isOutputGraphSpecified && inferGraphsFromAnnotatedModel) {
            log.error("Module is set to deploy in one context as well as infer contexts from annotated model. " +
                "Thus, ignoring deploy of triples.");
            return DatasetFactory.create();
        }
        if (isOutputGraphSpecified) {
            return DatasetFactory.create().addNamedModel(outputGraphId, model);
        }
        if (inferGraphsFromAnnotatedModel) {
            return createDatasetFromAnnotatedModel(model);
        }
        return DatasetFactory.create(model);
    }

    static Dataset createDatasetFromAnnotatedModel(@NotNull Model model) {
        Dataset dataset = DatasetFactory.create();
        JenaUtils.listStatementSubjectOfReifiedStatements(model).forEachRemaining(
            rs -> {
                rs.listProperties(KBSS_MODULE.JENA.is_part_of_graph)
                    .mapWith( gs -> gs.getObject().toString())
                        .forEachRemaining(
                            u ->  {
                                JenaUtils.addStatement(dataset.getNamedModel(u), rs);
                            }
                        );
            }
        );
        return dataset;
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadManualConfiguration() {
        if (repositoryManager != null && rdf4jServerURL != null) {
            throw new ModuleConfigurationInconsistentException(
                    "Repository manager is already initialized. Trying to override its configuration from RDF.");
        }
        repositoryManager = RepositoryProvider.getRepositoryManager(rdf4jServerURL);
        String username = getConfigurationVariable(rdf4jSecuredUsernameVariable);
        String password = getConfigurationVariable(rdf4jSecuredPasswordVariable);
        if (username != null && password != null) {
            RemoteRepositoryManager remoteRepositoryManager = (RemoteRepositoryManager) repositoryManager;
            remoteRepositoryManager.setUsernameAndPassword(username, password);
        }
        repository = repositoryManager.getRepository(rdf4jRepositoryName);
        log.debug("Inferring contexts from annotated input triples.");
    }
    private static @Nullable String getConfigurationVariable(String variableName) {
        if (variableName == null) {
            return null;
        }
        return CoreConfigProperies.getConfigurationVariable(variableName);
    }

    private boolean isRdf4jContextIRIDefined() {
        return (rdf4jContextIRI != null) && (!rdf4jContextIRI.isEmpty());
    }
}
