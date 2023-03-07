package cz.cvut.spipes.debug.persistance.dao;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.spipes.model.Transformation;

@Repository
public class TransformationDao {

    private final EntityManager em;

    @Autowired
    public TransformationDao(EntityManager em) {
        this.em = em;
    }

    public List<Transformation> findAll() {
        return em.createNamedQuery("Transformation.findAll", Transformation.class).getResultList();
    }

    public Transformation findByUri(String uri) {
        Objects.requireNonNull(uri);
        try {
            return em.find(Transformation.class, uri);
        } finally {
            em.close();
        }
    }

//    protected Transformation findByUsername(String username, EntityManager em) {
//        try {
//            return em.createNativeQuery("SELECT DISTINCT ?p WHERE { ?p ?hasUsername ?username .}", Person.class)
//                    .setParameter("hasUsername", URI.create(Vocabulary.s_p_accountName))
//                    .setParameter("username", username, Constants.LANGUAGE)
//                    .setDescriptor(new EntityDescriptor(Constants.PERSONS_CONTEXT))
//                    .getSingleResult();
//        } catch (NoResultException e) {
//            return null;
//        }
//    }
    /*
            SELECT ?subject ?predicate ?object ?context
        WHERE {
        GRAPH ?context {
            ?subject ?predicate ?object
            FILTER(REGEX(str(?context), "^http://onto.fel.cvut.cz/ontologies/dataset-descriptor/transformation/1678036284039000.*output$"))
            FILTER(?subject = <http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/aaaa-bbbbb>)
            FILTER(?predicate = <http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0.1/is-greeted-by-message>)
            FILTER(?object = "Hello AAAA bbbbb.")
     */
    public List<String> findContexts(String username) {
        try {
            return em.createNativeQuery("SELECT ?context WHERE { GRAPH ?context {?subject ?predicate ?object FILTER(REGEX(str(?context), \"^http://onto"
                            + ".fel.cvut.cz/ontologies/dataset-descriptor/transformation/1678036284039000.*output$\")) FILTER(?subject = <http://onto.fel.cvut"
                            + ".cz/ontologies/s-pipes/hello-world-example-0.1/aaaa-bbbbb>)  FILTER(?predicate = <http://onto.fel.cvut.cz/ontologies/s-pipes/hello-world-example-0"
                            + ".1/is-greeted-by-message>) FILTER(?object = \"Hello AAAA bbbbb.\")}}", String.class)
                    .getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
}
