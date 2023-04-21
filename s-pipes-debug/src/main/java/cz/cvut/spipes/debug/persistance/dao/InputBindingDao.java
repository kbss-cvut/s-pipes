package cz.cvut.spipes.debug.persistance.dao;

import static cz.cvut.spipes.engine.VariablesBinding.HAS_BOUND_VARIABLE;

import org.springframework.stereotype.Repository;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.spipes.model.Thing;

@Repository
public class InputBindingDao extends AbstractDao<Thing> {
    protected InputBindingDao(EntityManager em) {
        super(em);
    }

    public boolean askHasBoundVariable(String binding, String variableName) {
        String query = String.format("ASK { "
                + "  GRAPH <%s>{ "
                + "    ?s <%s> \"%s\""
                + "  }"
                + "}", binding, HAS_BOUND_VARIABLE, variableName);
        try {
            return (Boolean) em.createNativeQuery(query).getSingleResult();
        } catch (Exception e) {
            return false;
        }
    }

}
