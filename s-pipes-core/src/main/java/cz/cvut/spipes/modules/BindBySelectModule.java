package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.util.QueryUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.model.Select;

public class BindBySelectModule extends AnnotatedAbstractModule {

    private static final Logger log = LoggerFactory.getLogger(BindBySelectModule.class);

    @Parameter(iri = SML.selectQuery)
    private Select selectQuery;

    //sml:replace
    @Parameter(iri = SML.replace)
    private boolean isReplace = false;

    @Override
    public ExecutionContext executeSelf() {

        Query query = QueryUtils.createQuery(selectQuery);

        QuerySolution inputBindings = executionContext.getVariablesBinding().asQuerySolution();

        QueryExecution execution = QueryExecutionFactory.create(query, executionContext.getDefaultModel(), inputBindings);

        ResultSet resultSet = execution.execSelect();

        VariablesBinding variablesBinding = new VariablesBinding();

        if (!resultSet.hasNext()) {
            log.debug("\"{}\" query did not return any values.", getLabel());
        } else {
            QuerySolution qs = resultSet.next();

            variablesBinding = new VariablesBinding(qs);

            if (resultSet.hasNext()) {
                log.warn("\"{}\" query did not return unique value.  If it is correct, the query should be restricted by additional statement (e.g. \"LIMIT 1\"). Returning binding {}, ignoring binding {}", getLabel(), variablesBinding.asQuerySolution(), resultSet.next());
            }
        }

        return ExecutionContextFactory.createContext(
            this.createOutputModel(isReplace, ModelFactory.createDefaultModel()),
            variablesBinding
        );
    }

    @Override
    public String getTypeURI() {
        return SML.BindBySelect;
    }

    public Select getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(Select selectQuery) {
        this.selectQuery = selectQuery;
    }
}
