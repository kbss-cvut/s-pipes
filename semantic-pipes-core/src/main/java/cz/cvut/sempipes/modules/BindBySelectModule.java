package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Select;

/**
 * Created by Miroslav Blasko on 28.5.16.
 */
public class BindBySelectModule extends AbstractModule  {

    private Select selectQuery;

    @Override
    public ExecutionContext execute() {

        Query query = ARQFactory.get().createQuery(selectQuery);

        QueryExecution execution = QueryExecutionFactory.create(query, executionContext.getDefaultModel());

        QuerySolution qs = execution.execSelect().next();

        VariablesBinding variablesBinding = new VariablesBinding(qs);

        return ExecutionContextFactory.createContext(executionContext.getDefaultModel(), variablesBinding);
    }

    @Override
    public void loadConfiguration() {
        selectQuery = getPropertyValue(SML.selectQuery).asResource().as(Select.class);
    }

    public Select getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(Select selectQuery) {
        this.selectQuery = selectQuery;
    }
}
