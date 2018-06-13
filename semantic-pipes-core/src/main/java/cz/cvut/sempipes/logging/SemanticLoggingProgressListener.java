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
import cz.cvut.sempipes.util.TempFileUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemanticLoggingProgressListener implements ProgressListener {
    private static final Logger LOG =
        LoggerFactory.getLogger(SemanticLoggingProgressListener.class);

    private static Path root;

    /**
     * Maps pipeline executions and module executions to the transformation object.
     */
    private static final Map<String, Object> executionMap = new HashMap<>();

    private static final Map<String, EntityManager> entityManagerMap = new HashMap<>();

    private static final Map<Long, Path> logDir= new HashMap<>();

    private static final String P_HAS_PART =
        Vocabulary.ONTOLOGY_IRI_dataset_descriptor + "/has-part";
    private static final String P_HAS_NEXT =
        Vocabulary.ONTOLOGY_IRI_dataset_descriptor + "/has-next";

    static {
        final Map<String, String> props = new HashMap<>();
        props.put(JOPAPersistenceProperties.SCAN_PACKAGE, "cz.cvut.sempipes.model");
        props.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, "spipes");
        PersistenceFactory.init(props);
        try {
            root = Files.createTempDirectory(TempFileUtils.createTimestampFileName("-s-pipes-log-"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public void pipelineExecutionStarted(final long pipelineExecutionId) {
        Thing pipelineExecution = new Thing();
        pipelineExecution.setId(getPipelineExecutionIri(pipelineExecutionId));
        pipelineExecution.setTypes(Collections.singleton(Vocabulary.s_c_transformation));

        executionMap.put(pipelineExecution.getId(), pipelineExecution);
        final Path pipelineExecutionDir = root.resolve("pipeline-execution-" + pipelineExecutionId);
        pipelineExecutionDir.toFile().mkdir();
        logDir.put(pipelineExecutionId, pipelineExecutionDir);

        final EntityManager em = PersistenceFactory.createEntityManager();
        synchronized (em) {
            entityManagerMap.put(pipelineExecution.getId(), em);
            em.getTransaction().begin();
            em.merge(pipelineExecution,
                new EntityDescriptor(URI.create(pipelineExecution.getId())));
            em.getTransaction().commit();
        }
    }

    @Override public void pipelineExecutionFinished(final long pipelineExecutionId) {
        final EntityManager em = entityManagerMap.get(getPipelineExecutionIri(pipelineExecutionId));
        synchronized (em) {
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
    }

    @Override public void moduleExecutionStarted(final long pipelineExecutionId, final String moduleExecutionId,
                                                 final Module outputModule,
                                                 final ExecutionContext inputContext,
                                                 final String predecessorModuleExecutionId) {
        Transformation moduleExecution = new Transformation();
        moduleExecution.setId(getModuleExecutionIri(moduleExecutionId));
        executionMap.put(moduleExecution.getId(), moduleExecution);

        SourceDatasetSnapshot input = new SourceDatasetSnapshot();
        input.setId(
            saveModelToFile(getDir(pipelineExecutionId), "module-" + moduleExecutionId + "-input.ttl", inputContext.getDefaultModel()));
        moduleExecution.setHas_input(input);

        if (predecessorModuleExecutionId != null) {
            Map<String, Set<Object>> properties2 = new HashMap<>();
            properties2
                .put(P_HAS_NEXT, Collections.singleton(URI.create(getModuleExecutionIri(predecessorModuleExecutionId))));
            moduleExecution.setProperties(properties2);
        }
    }

    @Override public void moduleExecutionFinished(long pipelineExecutionId, final String moduleExecutionId,
                                                  final Module module) {
        final EntityManager em = entityManagerMap.get(getPipelineExecutionIri(pipelineExecutionId));
        Transformation moduleExecution =
            (Transformation) executionMap.get(getModuleExecutionIri(moduleExecutionId));

        Map<String, Set<Object>> properties = new HashMap<>();
        properties.put(P_HAS_PART, Collections.singleton(URI.create(moduleExecution.getId())));

        Thing output = new Thing();
        output.setId(saveModelToFile(getDir(pipelineExecutionId), "module-" + moduleExecutionId + "-output.ttl", module.getOutputContext().getDefaultModel()));
        moduleExecution.setHas_output(Collections.singleton(output));

        synchronized (em) {
            if (em.isOpen()) {
                em.getTransaction().begin();
                final Transformation pipelineExecution =
                    em.find(Transformation.class, getPipelineExecutionIri(pipelineExecutionId));
                final EntityDescriptor pd = new EntityDescriptor(URI.create(pipelineExecution.getId()));

                pipelineExecution.setProperties(properties);
                if (moduleExecution.getProperties() != null && moduleExecution.getProperties().containsKey(
                    P_HAS_NEXT)) {
                    String nextId = moduleExecution.getProperties().get(P_HAS_NEXT).iterator()
                                                   .next().toString();
                    Thing next = new Thing();
                    next.setId(nextId);
                    em.merge(next, pd);
                }

                final Thing input = moduleExecution.getHas_input();
                em.merge(input, pd);
                em.merge(output, pd);
                em.merge(moduleExecution, pd);
                em.merge(pipelineExecution, pd);
                em.getTransaction().commit();
            }
        }
    }

    private Path getDir(final long pipelineExecutionId) {
        return logDir.get(pipelineExecutionId);
    }

    private String saveModelToFile(Path dir, String fileName, Model model) {
        File file = null;
        try {
            file =
                Files.createFile(dir.resolve(TempFileUtils.createTimestampFileName(fileName))).toFile();
        } catch (IOException e) {
            LOG.error("Error during file creation.", e);
            return null;
        }
        try (OutputStream fileIs = new FileOutputStream(file)) {
            model.write(fileIs, FileUtils.langTurtle);
            return file.toURI().toURL().toString();
        } catch (IOException e) {
            LOG.error("Error during dataset snapshot saving.", e);
            return null;
        }
    }

    private String getPipelineExecutionIri(final long pipelineId) {
        return Vocabulary.s_c_transformation + "/" + pipelineId;
    }

    private String getModuleExecutionIri(final String moduleExecutionId) {
        return Vocabulary.s_c_transformation + "/" + moduleExecutionId;
    }
}
