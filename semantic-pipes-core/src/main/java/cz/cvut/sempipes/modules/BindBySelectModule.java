package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Select;

/**
 * Created by Miroslav Blasko on 28.5.16.
 */
public class BindBySelectModule extends AbstractModule  {

    private static final Logger LOG = LoggerFactory.getLogger(BindBySelectModule.class);
    private Select selectQuery;

    @Override
    public ExecutionContext executeSelf() {

        Query query = ARQFactory.get().createQuery(selectQuery);

        QuerySolution inputBindings = executionContext.getVariablesBinding().asQuerySolution();

        QueryExecution execution = QueryExecutionFactory.create(query, executionContext.getDefaultModel(), inputBindings);

        ResultSet resultSet = execution.execSelect();

        VariablesBinding variablesBinding = new VariablesBinding();

        if (! resultSet.hasNext()) {
            LOG.debug("\"{}\" query did not return any values.", getLabel());
        } else {
            QuerySolution qs = resultSet.next();

            variablesBinding = new VariablesBinding(qs);

            if (resultSet.hasNext()) {
                LOG.warn("\"{}\" query did not return unique value.  If it is correct, the query should be restricted by additional statement (e.g. \"LIMIT 1\"). Returning binding {}, ignoring binding {}", getLabel(), variablesBinding.asQuerySolution(), resultSet.next());
            }
        }

        return ExecutionContextFactory.createContext(executionContext.getDefaultModel(), variablesBinding);
    }

    @Override
    public String getTypeURI() {
        return SML.BindBySelect.getURI();
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
