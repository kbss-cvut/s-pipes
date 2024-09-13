package cz.cvut.spipes.modules;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.exceptions.CkanException;
import eu.trentorise.opendata.jackan.model.CkanCatalog;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanUser;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@SPipesModule(label = "ckan2rdf-v1", comment = "Convert ckan to rdf.")
public class Ckan2RdfModule extends AnnotatedAbstractModule {

    public static final String TYPE_URI = KBSS_MODULE.uri + "ckan2rdf-v1";
    public static final String NS_DDO = "http://onto.fel.cvut.cz/ontologies/dataset-descriptor/";

    @Parameter(iri = TYPE_URI + "/" + "p-ckan-url", comment = "URL of the CKAN server.")
    private String propCkanApiUrl;

    @Parameter(iri = TYPE_URI + "/" + "p-rdf4j-repository-url", comment = "URL of the RDF4J repository.")
    private String propRdf4jRepositoryUrl;

    // TODO - revise comment
    @Parameter(iri = TYPE_URI + "/" + "p-max-datasets", comment = "Limits the number of processed datasets.")
    private Integer maxDatasets = Integer.MAX_VALUE;

    private Resource createInstance(final String classIri, final Model m) {
        final Resource instance = ResourceFactory.createResource(
            classIri + "-" + Instant.now().toString());
        final Resource clazz = ResourceFactory.createResource(classIri);
        m.add(instance, RDF.type, clazz);
        return instance;
    }

    private CkanDataset processDataset(EntityManager em, final String dataset, CkanClient client,
                                       Resource indDescription, Resource indDatasetSnapshot) {
        final Resource indDatasetSnapshotSub = createInstance(
            NS_DDO + "dataset-snapshot",
            executionContext.getDefaultModel());
        executionContext.getDefaultModel().add(indDatasetSnapshot, ResourceFactory.createProperty(
            NS_DDO + "has-sub-dataset-snapshot"), indDatasetSnapshotSub);

        final Resource iDescriptionSub = createInstance(
            NS_DDO + "description",
            executionContext.getDefaultModel());
        executionContext.getDefaultModel().add(indDescription, ResourceFactory.createProperty(
            NS_DDO + "has-partial-description"), iDescriptionSub);
        executionContext.getDefaultModel().add(iDescriptionSub, ResourceFactory.createProperty(
            NS_DDO + "is-description-of"), indDatasetSnapshotSub);

        CkanDataset ckanDataset = null;
        try {
            ckanDataset = client.getDataset(dataset);
            executionContext.getDefaultModel().add(indDatasetSnapshotSub, DC.source,
                ResourceFactory.createResource(ckanDataset.getIri()));
            em.getTransaction().begin();
            em.merge(ckanDataset,
                new EntityDescriptor(URI.create(client.getCatalogUrl()))
            );
            em.getTransaction().commit();

            return ckanDataset;
        } catch (CkanException e) {
            executionContext.getDefaultModel().add(iDescriptionSub, ResourceFactory.createProperty(
                NS_DDO + "has-error-result"), e.getMessage());
            log.warn("{}: Problem during dataset fetch {}", dataset, e.getMessage(), e);
        }
        return null;
    }

    private void processUser(EntityManager em, final CkanUser user, CkanClient client,
                             Resource indDescription, Resource indDatasetSnapshot) {

        final Resource iDatasetSnapshotSub = createInstance(
            NS_DDO + "dataset-snapshot", executionContext.getDefaultModel());
        executionContext.getDefaultModel().add(indDatasetSnapshot, ResourceFactory.createProperty(
            NS_DDO + "has-sub-dataset-snapshot"), iDatasetSnapshotSub);
        executionContext.getDefaultModel().add(iDatasetSnapshotSub, DC.source,
            ResourceFactory.createResource(user.getIri()));

        final Resource iDescriptionSub = createInstance(
            NS_DDO + "description", executionContext.getDefaultModel());
        executionContext.getDefaultModel().add(indDescription, ResourceFactory.createProperty(
            NS_DDO + "has-partial-description"), iDescriptionSub);
        executionContext.getDefaultModel().add(iDescriptionSub, ResourceFactory.createProperty(
            NS_DDO + "is-description-of"), iDatasetSnapshotSub);

        em.getTransaction().begin();
        em.merge(user,
            new EntityDescriptor(URI.create(client.getCatalogUrl()))
        );
        em.getTransaction().commit();
    }

    @Override
    ExecutionContext executeSelf() {
        final Map<String, String> props = new HashMap<>();
        props.put(JOPAPersistenceProperties.SCAN_PACKAGE, "eu.trentorise.opendata.jackan.model");
        props.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, propRdf4jRepositoryUrl);
        PersistenceFactory.init(props);

        final Resource iDescription = createInstance(
            NS_DDO + "description", executionContext.getDefaultModel());
        final Resource iDatasetSnapshot = createInstance(
            NS_DDO + "dataset-snapshot", executionContext.getDefaultModel());
        executionContext.getDefaultModel().add(iDescription, ResourceFactory.createProperty(
            NS_DDO + "is-description-of"), iDatasetSnapshot);
        executionContext.getDefaultModel().add(iDescription, DC.source,
            ResourceFactory.createResource(propCkanApiUrl));
        executionContext.getDefaultModel().add(ResourceFactory.createResource(propCkanApiUrl),
            RDF.type, ResourceFactory.createResource(
                "http://onto.fel.cvut.cz/ontologies/org/ckan/catalog"));


        final EntityManager em = PersistenceFactory.createEntityManager();
        try {
            final CkanClient cc = new CkanClient(propCkanApiUrl);
            final AtomicInteger i = new AtomicInteger();
            i.set(0);

            CkanCatalog catalog = new CkanCatalog();
            catalog.setIri(cc.getCatalogUrl());
            catalog.setDatasets(new HashSet<>());

            List<String> datasets = new ArrayList<>();
            try {
                datasets = cc.getDatasetList();
            } catch (CkanException e) {
                log.warn("Problem during datasets fetch {}", e.getMessage(), e);
                executionContext.getDefaultModel().add(iDescription, ResourceFactory.createProperty(
                    NS_DDO + "has-error-result"), e.getMessage());
            }
            int max = datasets.size();
            for (final String dataset : datasets) {
                log.info("Processing Dataset {} / {} - {}", i.incrementAndGet(), max, dataset);
                CkanDataset ckanDataset = processDataset(em, dataset,
                    cc, iDescription, iDatasetSnapshot);
                if (ckanDataset != null) {
                    catalog.getDatasets().add(ckanDataset);
                    if (i.get() > maxDatasets) {
                        log.info("Breaking execution {} / {} ", i.get(), maxDatasets);
                        break;
                    }
                }
            }

            i.set(0);
            List<CkanUser> userList = new ArrayList<>();
            max = 0;
            try {
                userList = cc.getUserList();
            } catch (CkanException e) {
                executionContext.getDefaultModel().add(iDescription, ResourceFactory.createProperty(
                    NS_DDO + "has-error-result"), e.getMessage());
                log.warn("Problem during userlist fetch {}", e.getMessage(), e);
            }
            max = userList.size();
            for (final CkanUser user : userList) {
                log.info("Processing User {} / {} - {}", i.incrementAndGet(), max, user.getId());
                processUser(em, user, cc, iDescription, iDatasetSnapshot);
            }

            em.getTransaction().begin();
            em.merge(catalog,
                new EntityDescriptor(URI.create(cc.getCatalogUrl()))
            );
            em.getTransaction().commit();
        } catch (final Exception e) {
            executionContext.getDefaultModel().add(iDescription, ResourceFactory.createProperty(
                NS_DDO + "has-error-result"), e.getMessage());
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
