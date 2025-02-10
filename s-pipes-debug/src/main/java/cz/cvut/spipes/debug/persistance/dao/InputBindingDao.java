package cz.cvut.spipes.debug.persistance.dao;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.spipes.model.Thing;
import org.springframework.stereotype.Repository;

import static cz.cvut.spipes.engine.VariablesBinding.HAS_BOUND_VARIABLE;

@Repository
public class InputBindingDao extends AbstractDao<Thing> {
    protected InputBindingDao(EntityManager em) {
        super(em);
    }

    public boolean askHasBoundVariable(String binding, String variableName) {
        String query = String.format("""
                ASK {
                  GRAPH <%s>{
                    ?s <%s> "%s"
                  }
                }""", binding, HAS_BOUND_VARIABLE, variableName);
        try {
            return (Boolean) em.createNativeQuery(query).getSingleResult();
        } catch (Exception e) {
            return false;
        }
    }

}
