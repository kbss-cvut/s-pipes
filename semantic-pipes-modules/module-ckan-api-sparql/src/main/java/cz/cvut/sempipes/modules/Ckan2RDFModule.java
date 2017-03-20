package cz.cvut.sempipes.modules;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.exceptions.CkanException;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanUser;
import eu.trentorise.opendata.jackan.model.CkanCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Ckan2RDFModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(Ckan2RDFModule.class);

    public static final String TYPE_URI = KBSS_MODULE.uri + "ckan2rdf-v1";

    /**
     * URL of the CKAN server
     */
    @Parameter(urlPrefix = TYPE_URI+"/", name = "p-ckan-url")
    private String pCkanApiURL;

    /**
     * URL of the RDF4J repository
     */
    @Parameter(urlPrefix = TYPE_URI+"/", name = "p-rdf4j-repository-url")
    private String pRdf4jRepositoryURL;

    /**
     * URL of the RDF4J repository
     */
    @Parameter(urlPrefix = TYPE_URI+"/", name = "p-max-datasets")
    private Integer maxDatasets = Integer.MAX_VALUE;

    @Override
    ExecutionContext executeSelf() {
        final Map<String,String> props = new HashMap<>();
        props.put(JOPAPersistenceProperties.SCAN_PACKAGE, "eu.trentorise.opendata.jackan.model");
        props.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, pRdf4jRepositoryURL);
        PersistenceFactory.init(props);

        final EntityManager em = PersistenceFactory.createEntityManager();
        try {
            final CkanClient cc = new CkanClient(pCkanApiURL);
            final AtomicInteger i = new AtomicInteger();
            i.set(0);

            CkanCatalog catalog = new CkanCatalog();
            catalog.setIri(cc.getCatalogUrl());
            catalog.setDatasets(new HashSet<>());

            List<String> datasets = new ArrayList<>();
            try{
                datasets = cc.getDatasetList();
            } catch(CkanException e) {
                LOG.warn("Problem during datasets fetch {}",e.getMessage(),e);
            }
            int max = datasets.size();
            for (final String dataset : datasets) {
                LOG.info("Processing Dataset {} / {} - {}", i.incrementAndGet(), max, dataset);
                CkanDataset ckanDataset = null;
                try{
                    ckanDataset = cc.getDataset(dataset);
                    catalog.getDatasets().add(ckanDataset);
                    em.getTransaction().begin();
                    em.merge(ckanDataset
//                    , new EntityDescriptor(URI.create(cc.getCatalogUrl()))
                    );
                    em.getTransaction().commit();

                    if ( i.get() > maxDatasets) {
                        break;
                    }
                } catch(CkanException e) {
                    LOG.warn("{}: Problem during dataset fetch {}",dataset, e.getMessage(),e);
                }
            }

            i.set(0);
            List<CkanUser> userList = new ArrayList<>();
            max = 0;
            try{
                userList=cc.getUserList();
            } catch(CkanException e) {
                LOG.warn("Problem during userlist fetch {}",e.getMessage(),e);
            }
            max = userList.size();
            for (final CkanUser user : userList) {
                LOG.info("Processing User {} / {} - {}", i.incrementAndGet(), max, user.getId());
                em.getTransaction().begin();
                em.merge(user
//                    , new EntityDescriptor(URI.create(cc.getCatalogUrl()))
                );
                em.getTransaction().commit();
            }

            em.getTransaction().begin();
            em.merge(catalog
//                    , new EntityDescriptor(URI.create(cc.getCatalogUrl()))
            );
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return executionContext;
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }
}
