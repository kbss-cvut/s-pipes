package cz.cvut.sempipes.modules;

import cz.cvut.kbss.commons.io.NamedStream;
import cz.cvut.kbss.commons.io.zip.ZipSource;
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
import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.constants.KM_PARAM;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.modules.eccairs.EccairsAccessFactory;
import cz.cvut.sempipes.modules.eccairs.SesameDataDao;
import cz.cvut.sempipes.modules.eccairs.JopaPersistenceUtils;
import cz.cvut.sempipes.registry.StreamResource;
import cz.cvut.sempipes.registry.StreamResourceRegistry;
import cz.cvut.sempipes.util.JenaUtils;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.Scanner;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ModuleImportE5x extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleImportE5x.class);

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

        if ( "text/xml".equals( e5xResource.getContentType() ) || "application/xml".equals( e5xResource.getContentType()  )) {
            LOG.debug("File considered XML (Content Type: {})",e5xResource.getContentType());
            try {

                NamedStream e5xResourceStream = new NamedStream(
                e5xResource.getUri(),
                new ByteArrayInputStream(e5xResource.getContent())
            );
            // create factory to parse eccairs values
            E5XXMLParser e5xXMLParser = new E5XXMLParser(eaf);
                e5xXMLParser.parseDocument(e5xResourceStream);
                r = e5xXMLParser.getReport();
            } catch (IOException e) {
                LOG.error("I/O Exception during E5XML parsing.");
            }
        } else if ( "application/zip".equals( e5xResource.getContentType() ) || "application/octet-stream".equals( e5xResource.getContentType()  ) ||  e5xResource.getContentType()==null ||  e5xResource.getContentType().isEmpty() ){
            LOG.debug("File considered ZIP (Content Type: {})",e5xResource.getContentType());
            // ZIP by default
            NamedStream e5xResourceStream = null;
                e5xResourceStream = new NamedStream(
                        e5xResource.getUri(),
                        new ByteArrayInputStream(e5xResource.getContent())
                );

//            E5XXMLParser e5xXMLParser = new E5XXMLParser(eaf);
//
//            PrintWriter out = null;
//            try {
//                out = new PrintWriter("/home/kremep1/xxx");
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            out.print(e5xResource.getContent());
//            out.close();

//            PrintWriter out = null;
//            try {
//                out = new PrintWriter("/home/kremep1/xxxyyy");
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            out.print(IOUtils.new Base64InputStream(new ByteArrayInputStream(e5xResource.getContent().getBytes()))));
//            out.close();


//            E5XMLLoader loader = new E5XMLLoader(new NamedStream(e5xResource.getUri(), new Base64InputStream(new ByteArrayInputStream(e5xResource.getContent().getBytes()))), eaf);
            E5XMLLoader loader = new E5XMLLoader(e5xResourceStream, eaf);
//            loader = loader.prepareFor(e5xResourceStream);
            Stream<EccairsReport> s = loader.loadData();
            r = s.findFirst().get();
//            ZipInputStream zin = new ZipInputStream(e5xResource.getContent());
//
//
//            try {
//            for (ZipEntry zipEntry;(zipEntry = zin.getNextEntry()) != null; )
//            {
//                System.out.println("reading zipEntry " + zipEntry.getName());
////                Scanner sc = new Scanner(zin);
////                while (sc.hasNextLine())
////                {
////                    System.out.println(sc.nextLine());
////                }
////                System.out.println("reading " + zipEntry.getName() + " completed");
//            }
//            zin.close();
//
////            E5XXMLParser e5xXMLParser = new E5XXMLParser(eaf);
////                e5xXMLParser.parseDocument(e5xResourceStream);
////                r = e5xXMLParser.getReport();
//            } catch (IOException e) {
//                LOG.error("I/O Exception during E5XML parsing.");
//            }
//


        } else {
            LOG.debug("Unsupported Content Type {}",e5xResource.getContentType());
            return outputExecutionContext;
        }

        try {
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

            outputExecutionContext = ExecutionContextFactory.createContext(outputModel);

            sesameRepo.getConnection().close();
            sesameRepo.shutDown();
        } catch (RepositoryException e) {
            LOG.warn("Failed to close sesame repository connection", e);
        }
        return outputExecutionContext;
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.getURI() + "import-e5x";
    }

    @Override
    public void loadConfiguration() {
        String e5xResourceUriStr = getEffectiveValue(KM_PARAM.has_resource_uri).asLiteral().toString();
        e5xResource = getResourceByUri(e5xResourceUriStr);
    }

    public String getE5xResourceUri() {
        return e5xResource.getUri();
    }

    public StreamResource getE5xResource(){
        return e5xResource;
    }

    public void setE5xResourceUri(String e5xResourceUri) {
        e5xResource = getResourceByUri(e5xResourceUri);
    }

    public void setE5xResource(@NotNull StreamResource e5xResource) {
        this.e5xResource = e5xResource;
    }

    private @NotNull StreamResource getResourceByUri(@NotNull String e5xResourceUriStr) {

        StreamResource res = StreamResourceRegistry.getInstance().getResourceByUrl(e5xResourceUriStr);

        if (res == null) {
            throw new RuntimeException("Stream resource " + e5xResourceUriStr + " not found. "); // TODO specific exception
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
