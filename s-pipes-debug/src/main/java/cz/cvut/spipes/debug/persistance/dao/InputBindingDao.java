package cz.cvut.spipes.debug.persistance.dao;

import org.springframework.stereotype.Repository;

import cz.cvut.kbss.jopa.model.EntityManager;

@Repository
public class InputBindingDao extends AbstractDao {
    protected InputBindingDao(EntityManager em) {
        super(em);
    }

    public boolean askHasBoundVariable(String binding, String variableName) {
        String query = String.format("ASK { "
                + "  GRAPH <%s>{ "
                + "    ?s <http://onto.fel.cvut.cz/ontologies/s-pipes/has_bound_value> \"%s\""
                + "  }"
                + "}", binding, variableName);
        try {
            return (Boolean) em.createNativeQuery(query).getSingleResult();
        } catch (Exception e) {
            return false;
        }
    }

}
