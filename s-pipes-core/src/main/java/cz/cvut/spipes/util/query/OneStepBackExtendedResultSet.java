package cz.cvut.spipes.util.query;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.engine.binding.Binding;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ResultSet that extends the original result set by adding bindings from the previous query solution.
 * The previous query solution is available under the same variable name with the suffix "__previous".
 *
 * <p>Example use:</p>
 * <pre>
 *     ResultSet resultSet = ...;
 *     ResultSet extendedResultSet = new OneStepBackExtendedResultSet(resultSet);
 *     while (extendedResultSet.hasNext()) {
 *         QuerySolution querySolution = extendedResultSet.next();
 *         RDFNode currentResource = querySolution.get("resource");
 *         RDFNode previousResource = querySolution.get("resource__previous");
 *     }
 *     </pre>
 */
public class OneStepBackExtendedResultSet implements ResultSet  {

    private static final String PREVIOUS_BINDING_SUFFIX = "__previous";

    private final ResultSet resultSet;
    private final List<String> resultVars;

    private QuerySolution previousQuerySolution;

    public OneStepBackExtendedResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
        this.previousQuerySolution = new QuerySolutionMap();
        this.resultVars = getExtendedResultVars(resultSet.getResultVars());
    }

    private static List<String> getExtendedResultVars(List<String> originalResultVars) {
        originalResultVars.stream()
            .filter(s -> s.endsWith(PREVIOUS_BINDING_SUFFIX))
            .findAny()
            .ifPresent(s -> {
                throw new IllegalArgumentException(
                    "The result set already contains a variable with suffix " + PREVIOUS_BINDING_SUFFIX
                );
            });
        List<String> joinedList = new ArrayList<>();
        joinedList.addAll(originalResultVars);
        joinedList.addAll(originalResultVars.stream()
            .map(v -> v + PREVIOUS_BINDING_SUFFIX)
            .toList());
        return joinedList;
    }

    @Override
    public boolean hasNext() {
        return resultSet.hasNext();
    }

    @Override
    public QuerySolution next() {
        QuerySolution nextQuerySolution = resultSet.next();

        QuerySolutionMap querySolution = new QuerySolutionMap();
        querySolution.addAll(nextQuerySolution);
        previousQuerySolution.varNames().
            forEachRemaining(
            varName -> querySolution.add(varName + PREVIOUS_BINDING_SUFFIX, previousQuerySolution.get(varName))
        );
        previousQuerySolution = nextQuerySolution;
        return querySolution;
    }

    @Override
    public QuerySolution nextSolution() {
        return this.next();
    }

    @Override
    public Binding nextBinding() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getRowNumber() {
        return resultSet.getRowNumber();
    }

    @Override
    public List<String> getResultVars() {
        return resultVars;
    }

    @Override
    public Model getResourceModel() {
        return resultSet.getResourceModel();
    }

    @Override
    public void forEachRemaining(Consumer<? super QuerySolution> action) {
        resultSet.forEachRemaining(action);
    }

    @Override
    public void close() {
        resultSet.close();
    }
}
