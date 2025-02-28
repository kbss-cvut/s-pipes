package cz.cvut.spipes.logging;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.spipes.Vocabulary;
import cz.cvut.spipes.constants.SPIPES;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ProgressListener;
import cz.cvut.spipes.model.SourceDatasetSnapshot;
import cz.cvut.spipes.model.Thing;
import cz.cvut.spipes.model.Transformation;
import cz.cvut.spipes.modules.Module;
import cz.cvut.spipes.util.DateUtils;
import cz.cvut.spipes.util.JenaUtils;
import cz.cvut.spipes.util.Rdf4jUtils;
import cz.cvut.spipes.util.TempFileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFLanguages;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriterFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class AdvancedLoggingProgressListener implements ProgressListener {
    private static final Logger log =
        LoggerFactory.getLogger(AdvancedLoggingProgressListener.class);
    /**
     * Maps pipeline executions and module executions to the transformation object.
     */
    private static final Map<String, Object> executionMap = new HashMap<>();
    private static final Map<String, Object> metadataMap = new HashMap<>();
    private static final Map<String, EntityManager> entityManagerMap = new HashMap<>();
    private static final Map<Long, Path> logDir = new HashMap<>();
    private static final String P_HAS_PART =
        Vocabulary.ONTOLOGY_IRI_dataset_descriptor + "/has-part";
    private static final String P_HAS_NEXT =
        Vocabulary.ONTOLOGY_IRI_dataset_descriptor + "/has-next";
    private static final String P_HAS_INPUT_BINDDING =
        Vocabulary.ONTOLOGY_IRI_dataset_descriptor + "/has-input-binding";
    private static final String LOCAL_NAME = "advanced-logging-progress-listener";
    private static final String PREFIX_IRI = SPIPES.uri + LOCAL_NAME + "/";
    static final Property P_RDF4J_SERVER_URL = getParameter("p-rdf4j-server-url");
    static final Property P_METADATA_REPOSITORY_NAME = getParameter("p-metadata-repository-name");
    static final Property P_DATA_REPOSITORY_NAME = getParameter("p-data-repository-name");
    static final Property P_PIPELINE_EXECUTION_GROUP_ID = getParameter("p-execution-group-id");
    static final Property PIPELINE_EXECUTION_GROUP_ID = getParameter("has-pipeline-execution-group-id");

    static {
        final Map<String, String> props = new HashMap<>();
        props.put(JOPAPersistenceProperties.SCAN_PACKAGE, "cz.cvut.spipes.model");
        props.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, "spipes");
        PersistenceFactory.init(props);
    }

    private final String rdf4jServerUrl;
    private final String metadataRepositoryName;
    private final String dataRepositoryName;
    private final String pipelineExecutionGroupId;
    private EntityManagerFactory metadataEmf = null;
    private EntityManagerFactory dataEmf = null;

    public AdvancedLoggingProgressListener(Resource configResource) {
        rdf4jServerUrl = getStringPropertyValue(configResource, P_RDF4J_SERVER_URL);
        metadataRepositoryName = getStringPropertyValue(configResource, P_METADATA_REPOSITORY_NAME);
        dataRepositoryName = getStringPropertyValue(configResource, P_DATA_REPOSITORY_NAME);
        pipelineExecutionGroupId = getStringPropertyValue(configResource, P_PIPELINE_EXECUTION_GROUP_ID);

        if (
            (metadataRepositoryName != null)
                && (dataRepositoryName != null)
                && (!metadataRepositoryName.equals(dataRepositoryName))
            ) {
            throw new UnsupportedOperationException("Different values in parameters " + P_METADATA_REPOSITORY_NAME + " and " + P_DATA_REPOSITORY_NAME + " yet not supported.");
        }
    }


    private static Property getParameter(final String name) {
        return ResourceFactory.createProperty(PREFIX_IRI + name);
    }

    @Override
    public void pipelineExecutionStarted(final long pipelineExecutionId) {
        Thing pipelineExecution = new Thing();
        pipelineExecution.setId(getPipelineExecutionIri(pipelineExecutionId));
        pipelineExecution.setTypes(Collections.singleton(Vocabulary.s_c_transformation));

        executionMap.put(pipelineExecution.getId(), pipelineExecution);
        final Path pipelineExecutionDir = FileSystemLogger.resolvePipelineExecution(pipelineExecutionId);
        pipelineExecutionDir.toFile().mkdir();
        logDir.put(pipelineExecutionId, pipelineExecutionDir);

        final EntityManager metadataEM = getMetadataEmf().createEntityManager();
        synchronized (metadataEM) {
            persistPipelineExecutionStarted(metadataEM, pipelineExecutionId, pipelineExecution);
            entityManagerMap.put(pipelineExecution.getId(), metadataEM);
        }

//        final EntityManager em = PersistenceFactory.createEntityManager();
//        synchronized (em) {
//            persistPipelineExecutionStarted(em, pipelineExecution);
//        }
//        entityManagerMap.put(pipelineExecution.getId(), em);
    }

    private void persistPipelineExecutionStarted(final EntityManager em, long pipelineExecutionId, Thing pipelineExecution) {
        em.getTransaction().begin();

        // new
        Date startDate = new Date();
        addProperty(pipelineExecution, SPIPES.has_pipeline_execution_start_date, startDate);
        addProperty(pipelineExecution, SPIPES.has_pipeline_execution_start_date_unix, startDate.getTime());
        if (pipelineExecutionGroupId != null) {
            addProperty(pipelineExecution, PIPELINE_EXECUTION_GROUP_ID, pipelineExecutionGroupId);
        }


        final EntityDescriptor pd = new EntityDescriptor(URI.create(pipelineExecution.getId()));

        em.merge(pipelineExecution, pd);

        em.getTransaction().commit();

    }

    private void persistPipelineExecutionFinished2(final EntityManager em, final long pipelineExecutionId) {
        if (em.isOpen()) {
            final TurtleWriterFactory factory = new TurtleWriterFactory();
            try (FileOutputStream fos = new FileOutputStream(
                Files.createFile(getDir(pipelineExecutionId).resolve("log.ttl")).toFile())) {
                final TurtleWriter writer = (TurtleWriter) factory.getWriter(fos);
                writer.startRDF();
                RepositoryConnection con = em.unwrap(SailRepository.class).getConnection();
                final ValueFactory f = con.getValueFactory();
                final RepositoryResult<Statement> res = con
                    .getStatements(null, null, null, true, f.createIRI(getPipelineExecutionIri(pipelineExecutionId)));
                while (res.hasNext()) {
                    writer.handleStatement(res.next());
                }
                writer.endRDF();
            } catch (IOException e) {
                e.printStackTrace();
            }
            entityManagerMap.remove(em);
            em.close();
            logDir.remove(pipelineExecutionId);
        }
    }

    private void persistPipelineExecutionFinished(final EntityManager em, final long pipelineExecutionId) {
        if (em.isOpen()) {
            log.debug("Saving metadata about finished pipeline execution {}.", pipelineExecutionId);
            Date finishDate = new Date();
            em.getTransaction().begin();

            String pipelineExecutionIri = getPipelineExecutionIri(pipelineExecutionId);
            final EntityDescriptor pd = new EntityDescriptor(URI.create(pipelineExecutionIri));
            final Transformation pipelineExecution =
                em.find(Transformation.class, pipelineExecutionIri, pd);

            // new
            Date startDate = DateUtils.toDate(getSingletonPropertyValue(pipelineExecution, SPIPES.has_pipeline_execution_start_date));
            addProperty(pipelineExecution, SPIPES.has_pipeline_execution_finish_date, finishDate);
            addProperty(pipelineExecution, SPIPES.has_pipeline_execution_finish_date_unix, finishDate.getTime());
            addProperty(pipelineExecution, SPIPES.has_pipeline_execution_duration, computeDuration(startDate, finishDate));
            addProperty(pipelineExecution, SPIPES.has_pipeline_name, metadataMap.get(SPIPES.has_pipeline_name.toString()));

            em.getTransaction().commit();
            em.close();
        }
    }

    private void persistModuleExecutionStarted(final EntityManager em,
                                               final long pipelineExecutionId,
                                               final String moduleExecutionId,
                                               final Module outputModule,
                                               final ExecutionContext inputContext,
                                               final String predecessorModuleExecutionId) {
    }

    private void persistModuleExecutionFinished(final EntityManager em,
                                                long pipelineExecutionId,
                                                final String moduleExecutionId,
                                                final Module module) {
    }

    @Override
    public void pipelineExecutionFinished(final long pipelineExecutionId) {
        final EntityManager em = entityManagerMap.get(getPipelineExecutionIri(pipelineExecutionId));

        synchronized (em) {
            persistPipelineExecutionFinished(em, pipelineExecutionId);
            entityManagerMap.remove(em);
            executionMap.remove(getPipelineExecutionIri(pipelineExecutionId));
        }
    }

    @Override
    public void moduleExecutionStarted(final long pipelineExecutionId, final String moduleExecutionId,
                                       final Module outputModule,
                                       final ExecutionContext inputContext,
                                       final String predecessorModuleExecutionId) {
        // construct model
        Transformation moduleExecution = new Transformation();
        moduleExecution.setId(getModuleExecutionIri(moduleExecutionId));

        SourceDatasetSnapshot input = new SourceDatasetSnapshot();

        input.setId(
            getModulesSourceDatasetSnapshotUrl(pipelineExecutionId, moduleExecutionId, SnapshotRole.INPUT_GRAPH)
        );
        moduleExecution.setHas_input(input);

        if (predecessorModuleExecutionId != null) {
            addProperty(
                moduleExecution,
                ResourceFactory.createProperty(P_HAS_NEXT),
                URI.create(getModuleExecutionIri(predecessorModuleExecutionId)));
        }

        // new
        addProperty(moduleExecution, SPIPES.has_module_id, URI.create(outputModule.getResource().getURI()));
        addProperty(moduleExecution, SPIPES.has_module_type, URI.create(outputModule.getTypeURI()));
        Date startDate = new Date();
        addProperty(moduleExecution, SPIPES.has_module_execution_start_date, startDate);
        addProperty(moduleExecution, SPIPES.has_module_execution_start_date_unix, startDate.getTime());
        addProperty(moduleExecution, SPIPES.has_input_model_triple_count, inputContext.getDefaultModel().size());

        // put model to map
        executionMap.put(moduleExecution.getId(), moduleExecution);

        // save metadata

        // save data
        saveModelToFile(moduleExecution.getHas_input().getId(), inputContext.getDefaultModel());
    }

    @Override
    public void moduleExecutionFinished(long pipelineExecutionId, final String moduleExecutionId,
                                        final Module module) {

        final EntityManager em = entityManagerMap.get(getPipelineExecutionIri(pipelineExecutionId));

        // retrieve model
        Transformation moduleExecution =
            (Transformation) executionMap.get(getModuleExecutionIri(moduleExecutionId));

        // construct model
        Map<String, Set<Object>> properties = new HashMap<>();
        properties.put(P_HAS_PART, Collections.singleton(URI.create(moduleExecution.getId())));

        Thing output = new Thing();
        output.setId(
            getModulesSourceDatasetSnapshotUrl(pipelineExecutionId, moduleExecutionId, SnapshotRole.OUTPUT_GRAPH)
        );
        moduleExecution.setHas_output(Collections.singleton(output));

        synchronized (em) {
            if (em.isOpen()) {
                Date finishDate = new Date();
                em.getTransaction().begin();
                String pipelineExecutionIri = getPipelineExecutionIri(pipelineExecutionId);
                final EntityDescriptor pd = new EntityDescriptor(URI.create(pipelineExecutionIri));
                final Transformation pipelineExecution =
                    em.find(Transformation.class, pipelineExecutionIri, pd);

                pipelineExecution.setProperties(properties);

                if (moduleExecution.getProperties() != null && moduleExecution.getProperties().containsKey(
                    P_HAS_NEXT)) {
                    String nextId = moduleExecution.getProperties().get(P_HAS_NEXT).iterator()
                        .next().toString();
                    Thing next = new Thing();
                    next.setId(nextId);
                    em.merge(next, pd);
                }

                // new
                Date startDate = (Date) getSingletonPropertyValue(moduleExecution, SPIPES.has_module_execution_start_date);
                addProperty(moduleExecution, SPIPES.has_module_execution_finish_date, finishDate);
                addProperty(moduleExecution, SPIPES.has_module_execution_finish_date_unix, finishDate.getTime());
                addProperty(moduleExecution, SPIPES.has_module_execution_duration, computeDuration(startDate, finishDate));
                addProperty(moduleExecution, SPIPES.has_output_model_triple_count, module.getOutputContext().getDefaultModel().size());
                addProperty(moduleExecution, SPIPES.has_pipeline_name, module.getResource().toString().replaceAll("\\/[^.]*$", ""));
                if(!metadataMap.containsKey(SPIPES.has_pipeline_name.toString())){
                    metadataMap.put(SPIPES.has_pipeline_name.toString(), module.getResource().toString().replaceAll("\\/[^.]*$", ""));
                }

                // input binding
                SourceDatasetSnapshot inputBindings = new SourceDatasetSnapshot(); //TODO type is not saved
                inputBindings.setId(
                    getModulesSourceDatasetSnapshotUrl(pipelineExecutionId, moduleExecutionId, SnapshotRole.INPUT_BINDING)
                );
                addProperty(
                    moduleExecution,
                    ResourceFactory.createProperty(P_HAS_INPUT_BINDDING),
                    URI.create(inputBindings.getId())
                );


                final Thing input = moduleExecution.getHas_input();
                em.merge(input, pd);
                em.merge(output, pd);
                em.merge(moduleExecution, pd);
                em.merge(inputBindings, pd);

                // save metadata
                Model ibModel = module.getExecutionContext().getVariablesBinding().getModel();

                writeRawData(em, URI.create(inputBindings.getId()), ibModel);


                em.getTransaction().commit();
            }
        }

        // save data
        saveModelToFile(output.getId(), module.getOutputContext().getDefaultModel());
    }

    private void writeRawData(EntityManager em, URI contextUri, Model model) {

        RepositoryConnection connection = null;
        try {
            StringWriter w = new StringWriter();
            model.write(w, RDFLanguages.NTRIPLES.getName());

            connection = em.unwrap(Repository.class).getConnection();

            connection.begin();
            connection.add(
                new StringReader(w.getBuffer().toString()),
                "",
                RDFFormat.N3,
                connection.getValueFactory().createIRI(contextUri.toString()));
            connection.commit();
        } catch (final RepositoryException | RDFParseException | RepositoryConfigException | IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        }
    }


    String getModulesSourceDatasetSnapshotUrl(final long pipelineExecutionId, final String moduleExecutionId, SnapshotRole snapshotRole) {
        try {
            return new File(getDir(pipelineExecutionId) + "/" + TempFileUtils.createTimestampFileName("-module-" + moduleExecutionId + "-" + snapshotRole + ".ttl")).toURI().toURL().toString();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path getDir(final long pipelineExecutionId) {
        return logDir.get(pipelineExecutionId);
    }


    private void saveModelToFile(String filePath, Model model) {
        File file = Paths.get(URI.create(filePath)).toFile();
        try (OutputStream fileIs = new FileOutputStream(file)) {
            JenaUtils.write(fileIs, model);
        } catch (IOException e) {
            log.error("Error during dataset snapshot saving.", e);
        }
    }

    private String getPipelineExecutionIri(final long pipelineId) {
        return Vocabulary.s_c_transformation + "/" + pipelineId;
    }

    private String getModuleExecutionIri(final String moduleExecutionId) {
        return Vocabulary.s_c_transformation + "/" + moduleExecutionId;
    }

    private EntityManagerFactory getMetadataEmf() {
        if (metadataEmf == null) {
            if ((rdf4jServerUrl != null) && (metadataRepositoryName != null)) {
                Rdf4jUtils.createRdf4RepositoryIfNotExist(rdf4jServerUrl, metadataRepositoryName);
                metadataEmf = RDF4JPersistenceFactory.getEntityManagerFactory(
                    "executionMetadataPU", rdf4jServerUrl, metadataRepositoryName);
            }
        }
        return metadataEmf;
    }

    private EntityManagerFactory getDataEmf() {
        if (dataEmf == null) {
            if ((rdf4jServerUrl != null) && (dataRepositoryName != null)) {
                Rdf4jUtils.createRdf4RepositoryIfNotExist(rdf4jServerUrl, dataRepositoryName);
                dataEmf = RDF4JPersistenceFactory.getEntityManagerFactory(
                    "executionDataPU", rdf4jServerUrl, dataRepositoryName);
            }
        }
        return dataEmf;
    }

    private String getStringPropertyValue(@NotNull Resource resource, @NotNull Property property) {

        org.apache.jena.rdf.model.Statement st = resource.getProperty(property);
        if (st == null) {
            return null;
        }
        return resource.getProperty(property).getObject().toString();
    }

    private void addProperty(@NotNull Thing thing, @NotNull Property property, @NotNull Object value) {
        if (thing.getProperties() == null) {
            thing.setProperties(new HashMap<>());
        }
        Map<String, Set<Object>> props = thing.getProperties();
        String key = property.toString();
        if (props.containsKey(key)) {
            props.get(key).add(value);
        } else {
            props.put(key, new HashSet<>(Collections.singleton(value)));
        }
    }

    private boolean hasProperty(@NotNull Thing thing, @NotNull Property property) {
        if (thing.getProperties() == null) {
            return false;
        }
        if (!thing.getProperties().containsKey(property.toString())) {
            return false;
        }
        if (thing.getProperties().get(property.toString()).isEmpty()) {
            return false;
        }
        return true;
    }

    private Object getSingletonPropertyValue(@NotNull Thing thing, @NotNull Property property) {
        if (hasProperty(thing, property)) {
            Set<Object> valueSet = thing.getProperties().get(property.toString());
            if (valueSet.size() != 1) {
                throw new IllegalStateException("Property " + property + " has multiple values.");
            }
            return valueSet.iterator().next();
        }
        return null;
    }

    private Long computeDuration(Date oldDate, Date newDate) {
        return (newDate.getTime() - oldDate.getTime());
    }

    private enum SnapshotRole {
        INPUT_GRAPH("input"),
        OUTPUT_GRAPH("output"),
        INPUT_BINDING("input-binding"),
        OUTPUT_BINDING("output-binding");

        private final String name;

        SnapshotRole(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }


}
