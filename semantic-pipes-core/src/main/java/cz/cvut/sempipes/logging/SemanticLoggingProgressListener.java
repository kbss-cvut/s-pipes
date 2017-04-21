package cz.cvut.sempipes.logging;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.sempipes.Vocabulary;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ProgressListener;
import cz.cvut.sempipes.model.Thing;
import cz.cvut.sempipes.model.transformation;
import cz.cvut.sempipes.modules.Module;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Collection;
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
    private static final Logger LOG = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Maps pipeline executions and module executions to the transformation object.
     */
    private static final Map<String, Object> executionMap = new HashMap<>();

    private static final Map<String, EntityManager> entityManagerMap = new HashMap<>();

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
                Files.createTempFile(Instant.now().toString() + "-pipeline-execution-", ".ttl")
                    .toFile());
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
    public void moduleExecutionStarted(final long pipelineId, final String moduleId, final Module outputModule, final ExecutionContext inputContext, final String predecessorId) {
        final EntityManager em = entityManagerMap.get(getPipelineIri(pipelineId));
        transformation moduleExecution = new transformation();
        moduleExecution.setId(getModuleExecutionIri(moduleId));
        final transformation pipelineExecution = em.find(transformation.class,getPipelineIri(pipelineId));
        Map<String,Set<String>> properties = new HashMap<>();
        properties.put("http://onto.fel.cvut.cz/ontologies/dataset-descriptor/has-part",Collections.singleton(moduleExecution.getId()));
        pipelineExecution.setProperties(properties);
        executionMap.put(moduleExecution.getId(), moduleExecution);

        em.getTransaction().begin();
        Thing input = new Thing();
        input.setId(saveModelToTemporaryFile(inputContext.getDefaultModel()));
        moduleExecution.setHas_input(Collections.singleton(input));

        if ( predecessorId != null) {
            Thing next = new Thing();
            next.setId(getModuleExecutionIri(predecessorId));
            Map<String,Set<String>> properties2 = new HashMap<>();
            properties2.put("http://onto.fel.cvut.cz/ontologies/dataset-descriptor/has-next",Collections.singleton(next.getId()));
            moduleExecution.setProperties(properties2);
            em.merge(next, new EntityDescriptor(URI.create(moduleExecution.getId())));
        }

        em.merge(pipelineExecution, new EntityDescriptor(URI.create(pipelineExecution.getId())));
        em.merge(input, new EntityDescriptor(URI.create(moduleExecution.getId())));
    }

    @Override
    public void moduleExecutionFinished(long pipelineId, final String moduleId, final Module module) {
        final EntityManager em = entityManagerMap.get(getPipelineIri(pipelineId));
        transformation moduleExecution = (transformation) executionMap.get(getModuleExecutionIri(moduleId));
        Thing output = new Thing();
        output.setId(saveModelToTemporaryFile(module.getOutputContext().getDefaultModel()));
        moduleExecution.setHas_output(Collections.singleton(output));
        em.merge(output, new EntityDescriptor(URI.create(moduleExecution.getId())));
        em.merge(moduleExecution, new EntityDescriptor(URI.create(moduleExecution.getId())));
        em.getTransaction().commit();
    }

    private String saveModelToTemporaryFile(Model model) {
        File tempFile = null;
        try {
            tempFile = Files.createTempFile(
                Instant.now().toString() + "-dataset-snapshot-", ".ttl").toFile();
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
