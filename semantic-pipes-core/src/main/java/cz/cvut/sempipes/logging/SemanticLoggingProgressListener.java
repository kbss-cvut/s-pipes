package cz.cvut.sempipes.logging;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.sempipes.Vocabulary;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ProgressListener;
import cz.cvut.sempipes.model.SourceDatasetSnapshot;
import cz.cvut.sempipes.model.Thing;
import cz.cvut.sempipes.model.Transformation;
import cz.cvut.sempipes.modules.Module;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemanticLoggingProgressListener implements ProgressListener {
    private static final Logger LOG
        = LoggerFactory.getLogger(SemanticLoggingProgressListener.class);

    /**
     * Maps pipeline executions and module executions to the transformation object.
     */
    private static final Map<String, Object> executionMap = new HashMap<>();

    private static final Map<String, EntityManager> entityManagerMap = new HashMap<>();

    private static final String P_HAS_PART
        = Vocabulary.ONTOLOGY_IRI_dataset_descriptor + "/has-part";
    private static final String P_HAS_NEXT
        = Vocabulary.ONTOLOGY_IRI_dataset_descriptor + "/has-next";

    static {
        final Map<String, String> props = new HashMap<>();
        props.put(JOPAPersistenceProperties.SCAN_PACKAGE, "cz.cvut.sempipes.model");
        props.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, "spipes");
        PersistenceFactory.init(props);
    }

    @Override
    public void pipelineExecutionStarted(final long pipelineId) {
        Thing pipelineExecution = new Thing();
        pipelineExecution.setId(getPipelineIri(pipelineId));
        pipelineExecution.setTypes(Collections.singleton(Vocabulary.s_c_transformation));

        final EntityManager em = PersistenceFactory.createEntityManager();
        executionMap.put(pipelineExecution.getId(), pipelineExecution);
        entityManagerMap.put(pipelineExecution.getId(), em);

        em.getTransaction().begin();
        em.merge(pipelineExecution, new EntityDescriptor(URI.create(pipelineExecution.getId())));
        em.getTransaction().commit();
    }

    @Override
    public void pipelineExecutionFinished(final long pipelineId) {
        final EntityManager em = entityManagerMap.get(getPipelineIri(pipelineId));
        final TurtleWriterFactory factory = new TurtleWriterFactory();
        try {
            FileOutputStream fos = new FileOutputStream(
                Files.createTempFile(
                    Instant.now().toString() + "-pipeline-execution-", ".ttl")
                    .toFile()
            );
            final TurtleWriter writer = (TurtleWriter) factory.getWriter(fos);
            writer.startRDF();
            final RepositoryResult<Statement> res = em.unwrap(SailRepository.class)
                .getConnection().getStatements(null, null, null, true);
            while (res.hasNext()) {
                writer.handleStatement(res.next());
            }
            writer.endRDF();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        entityManagerMap.remove(em);
        em.close();
    }

    @Override
    public void moduleExecutionStarted(final long pipelineId,
                                       final String moduleId,
                                       final Module outputModule,
                                       final ExecutionContext inputContext,
                                       final String predecessorId) {
        Transformation moduleExecution = new Transformation();
        moduleExecution.setId(getModuleExecutionIri(moduleId));
        executionMap.put(moduleExecution.getId(), moduleExecution);

        SourceDatasetSnapshot input = new SourceDatasetSnapshot();
        input.setId(saveModelToTemporaryFile(inputContext.getDefaultModel()));
        moduleExecution.setHas_input(input);

        if (predecessorId != null) {
            Map<String, Set<String>> properties2 = new HashMap<>();
            properties2.put(P_HAS_NEXT,
                Collections.singleton(getModuleExecutionIri(predecessorId)));
            moduleExecution.setProperties(properties2);
        }
    }

    @Override
    public void moduleExecutionFinished(long pipelineId,
                                        final String moduleId,
                                        final Module module) {
        final EntityManager em = entityManagerMap.get(getPipelineIri(pipelineId));
        Transformation moduleExecution = (Transformation) executionMap
            .get(getModuleExecutionIri(moduleId));

        final Transformation pipelineExecution = em.find(Transformation.class,
            getPipelineIri(pipelineId));
        Map<String, Set<String>> properties = new HashMap<>();
        properties.put(P_HAS_PART, Collections.singleton(moduleExecution.getId()));
        pipelineExecution.setProperties(properties);

        Thing output = new Thing();
        output.setId(saveModelToTemporaryFile(module.getOutputContext().getDefaultModel()));
        moduleExecution.setHas_output(Collections.singleton(output));

        em.getTransaction().begin();
        if (moduleExecution.getProperties() != null && moduleExecution.getProperties()
            .containsKey(P_HAS_NEXT)) {
            String nextId = moduleExecution.getProperties().get(P_HAS_NEXT).iterator().next();
            Thing next = new Thing();
            next.setId(nextId);
            em.merge(next, new EntityDescriptor(URI.create(moduleExecution.getId())));
        }

        Thing input = moduleExecution.getHas_input();
        em.merge(input, new EntityDescriptor(URI.create(moduleExecution.getId())));
        em.merge(output, new EntityDescriptor(URI.create(moduleExecution.getId())));
        em.merge(moduleExecution, new EntityDescriptor(URI.create(moduleExecution.getId())));
        em.merge(pipelineExecution, new EntityDescriptor(URI.create(pipelineExecution.getId())));
        em.getTransaction().commit();
    }

    private String saveModelToTemporaryFile(Model model) {
        File tempFile = null;
        try {
            tempFile = Files.createTempFile(Instant.now().toString()
                + "-dataset-snapshot-", ".ttl").toFile();
        } catch (IOException e) {
            LOG.error("Error during temporary file creation.", e);
            return null;
        }
        try (OutputStream tempFileIs = new FileOutputStream(tempFile)) {
            model.write(tempFileIs, FileUtils.langTurtle);
            return tempFile.toURI().toURL().toString();
        } catch (IOException e) {
            LOG.error("Error during dataset snapshot saving.", e);
            return null;
        }
    }

    private String getPipelineIri(final long pipelineId) {
        return Vocabulary.s_c_transformation + "/" + pipelineId;
    }

    private String getModuleExecutionIri(final String moduleExecutionId) {
        return Vocabulary.s_c_transformation + "/" + moduleExecutionId;
    }
}
