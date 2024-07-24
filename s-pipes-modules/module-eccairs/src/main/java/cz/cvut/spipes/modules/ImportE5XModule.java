package cz.cvut.spipes.modules;

import cz.cvut.kbss.commons.io.NamedStream;
import cz.cvut.kbss.eccairs.report.e5xml.E5XMLLoader;
import cz.cvut.kbss.eccairs.report.e5xml.e5x.E5XXMLParser;
import cz.cvut.kbss.eccairs.report.model.EccairsReport;
import cz.cvut.kbss.eccairs.report.model.dao.EccairsReportDao;
import cz.cvut.kbss.eccairs.schema.dao.SingeltonEccairsAccessFactory;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.ucl.MappingEccairsData2Aso;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.exception.ResourceNotFoundException;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.modules.eccairs.EccairsAccessFactory;
import cz.cvut.spipes.modules.eccairs.JopaPersistenceUtils;
import cz.cvut.spipes.modules.eccairs.SesameDataDao;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StreamResourceRegistry;
import cz.cvut.spipes.util.JenaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.jetbrains.annotations.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

@Slf4j
@SPipesModule(label = "import e5x", comment = "Convert e5x xml files to rdf.")
public class ImportE5XModule extends AbstractModule {

    // TODO - this parameter id defined with IRI <http://onto.fel.cvut.cz/ontologies/lib/module-param/has-resource-uri> in  s-pipes-modules\module.sms.ttl
    // TODO - we should be able to annotate directly "StreamResource e5xResource" instead
    @Parameter(name = "has-resource-uri", comment = "Uri of a resource referencing content of an e5x file.")
    private String e5xResourceUriStr;

    StreamResource e5xResource;

    private boolean computeEccairsToAviationSafetyOntologyMapping = true;

    @Override
    ExecutionContext executeSelf() {

        ExecutionContext outputExecutionContext = ExecutionContextFactory.createEmptyContext();
        // get e5x resource

        // create eccairs schema factory
        SingeltonEccairsAccessFactory eaf = EccairsAccessFactory.getInstance();

        // create entity manager factory with a memory non-persistent sesame repository
        EntityManagerFactory emf = JopaPersistenceUtils.createEntityManagerFactoryWithMemoryStore();

        EccairsReport r = null;

        final NamedStream e5xResourceStream = new NamedStream(e5xResource.getUri()+".e5x", new ByteArrayInputStream(e5xResource.getContent()));

        try {
            if ("text/xml".equals(e5xResource.getContentType()) || "application/xml".equals(e5xResource.getContentType())) {
                log.debug("File considered XML (Content Type: {})", e5xResource.getContentType());
                log.debug("- content length: {}, content (as string) : ", e5xResource.getContent().length, new String(e5xResource.getContent()));
                // create factory to parse eccairs values
                final E5XXMLParser e5xXMLParser = new E5XXMLParser(eaf);
                e5xXMLParser.parseDocument(e5xResourceStream);
                r = e5xXMLParser.getReport();
            } else if ("application/zip".equals(e5xResource.getContentType()) || "application/octet-stream".equals(e5xResource.getContentType()) || e5xResource.getContentType() == null || e5xResource.getContentType().isEmpty()) {
                log.debug("File considered ZIP (Content Type: {})", e5xResource.getContentType());
                log.debug("- content length: {}, content (as byte array): {}",e5xResource.getContent().length, Arrays.toString(e5xResource.getContent()));
                // ZIP by default
                final E5XMLLoader loader = new E5XMLLoader(e5xResourceStream, eaf);
                log.debug("- loader created based on resource stream name:{}, email:{}, stream:{}, closed: {}", e5xResourceStream.getName(), e5xResourceStream.getEmailId(), e5xResourceStream.getContent(), e5xResourceStream.isCloased());
                EccairsReport[] s = loader.loadData().toArray(EccairsReport[]::new);

                log.debug("- found {} reports", s.length);
                if ( s.length > 0 ) {
                    r = s[0];
                }
            } else {
                log.debug("Unsupported Content Type {}", e5xResource.getContentType());
                return outputExecutionContext;
            }

            if ( r == null ) {
                log.debug("No report parsed, terminating.");
                return outputExecutionContext;
            }

            String reportContext = EccairsReport.createContextURI(e5xResource.getUri());
            r.setUri(reportContext);

            Descriptor d = new EntityDescriptor(URI.create(reportContext));
            EntityManager em = emf.createEntityManager();
            EccairsReportDao dao = new EccairsReportDao(em);

            // persisting the parsed report
            em.getTransaction().begin();
            dao.safePersist(r, d);
            em.getTransaction().commit();// the transanction needs to be commited. The updates operate on the persisted report.

            if (computeEccairsToAviationSafetyOntologyMapping) {

                // create the class for the mappings between eccairs and aso
                MappingEccairsData2Aso mapping = new MappingEccairsData2Aso(eaf);

                em.getTransaction().begin();
                mapping.mapReport(r, em, d.toString());
                em.getTransaction().commit();
            }

//        em.getTransaction().begin();
//        r = em.find(EccairsReport.class, r.getUri());
//        em.remove(r);
//        em.getTransaction().commit();
            Repository sesameRepo = JopaPersistenceUtils.getRepository(em);

            String transformedModelText = SesameDataDao.getRepositoryData(sesameRepo, URI.create(reportContext));

            Model outputModel = JenaUtils.readModelFromString(transformedModelText, FileUtils.langXML);

            removeDefaultPrefix(outputModel);

            outputExecutionContext = ExecutionContextFactory.createContext(outputModel);

            sesameRepo.getConnection().close();
            sesameRepo.shutDown();
        } catch (IOException e) {
            log.warn("An exception occurred during report processing.", e);
        } catch (RepositoryException e) {
            log.warn("Failed to close sesame repository connection", e);
        }
        return outputExecutionContext;
    }

    private void removeDefaultPrefix(Model outputModel) {
        outputModel.removeNsPrefix("");
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.getURI() + "import-e5x";
    }

    @Override
    public void loadConfiguration() {
        e5xResourceUriStr = getEffectiveValue(KBSS_MODULE.has_resource_uri).asLiteral().toString();
        e5xResource = getResourceByUri(e5xResourceUriStr);
    }

    public String getE5xResourceUri() {
        return e5xResource.getUri();
    }

    public StreamResource getE5xResource() {
        return e5xResource;
    }

    public void setE5xResourceUri(String e5xResourceUri) {
        e5xResource = getResourceByUri(e5xResourceUri);
    }

    public void setE5xResource(@NotNull StreamResource e5xResource) {
        this.e5xResource = e5xResource;
    }

    @NotNull
    private StreamResource getResourceByUri(@NotNull String e5xResourceUriStr) {

        StreamResource res = StreamResourceRegistry.getInstance().getResourceByUrl(e5xResourceUriStr);

        if (res == null) {
            throw new ResourceNotFoundException("Stream resource " + e5xResourceUriStr + " not found. ");
        }
        return res;
    }

    public boolean isComputeEccairsToAviationSafetyOntologyMapping() {
        return computeEccairsToAviationSafetyOntologyMapping;
    }

    public void setComputeEccairsToAviationSafetyOntologyMapping(boolean computeEccairsToAviationSafetyOntologyMapping) {
        this.computeEccairsToAviationSafetyOntologyMapping = computeEccairsToAviationSafetyOntologyMapping;
    }
}
