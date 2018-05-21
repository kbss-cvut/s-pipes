package cz.cvut.sempipes.util;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.mgt.Explain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryUtils {

    private static final Logger LOG = LoggerFactory.getLogger(QueryUtils.class);

    /**
     * Returns new query by substituting marker within given query with given value.
     * Marker must be in syntax #${MARKER_NAME}.
     * For example for marker with name "VALUES" query can look like following one :
     * SELECT * {
     * #${VALUES}
     * }
     *
     * @param markerName    name of the marker
     * @param replacedValue replacement of the marker
     * @param query         query with the marker
     * @return new query with replaced value in place of the marker
     */
    public static String substituteMarkers(String markerName, String replacedValue, String query) {
        return query.replaceAll("\\s*#\\s*\\$\\{" + markerName + "\\}", Matcher.quoteReplacement(replacedValue));
    }


    public static String nextResultsToValuesClause(ResultSet resultSet, int rowsCount) {
        StringBuffer clauseBuffer = new StringBuffer();
        clauseBuffer
            .append("\n")
            .append(getValuesClauseHeader(resultSet))
            .append(getValuesClauseValues(resultSet, rowsCount))
            .append("}\n");

        return clauseBuffer.toString();
    }

    public static String serializeToSparql(RDFNode rdfNode) {
        ParameterizedSparqlString pss= new ParameterizedSparqlString();
        pss.appendNode(rdfNode);
        return pss.toString();
    }



    private static String getValuesClauseHeader(ResultSet resultSet) {
        return resultSet.getResultVars().stream()
            .map(v -> "?" + v)
            .collect(Collectors.joining(" ", "VALUES (", ") {\n"));
    }

    private static String getValuesClauseValues(ResultSet resultSet, int rowsCount) {

        StringBuffer valuesBuffer = new StringBuffer();

        while (resultSet.hasNext() && rowsCount > 0) {
            rowsCount--;

            QuerySolution querySolution = resultSet.next();

            valuesBuffer.append(
                resultSet.getResultVars().stream()
                    .map(querySolution::get)
                    .map(QueryUtils::serializeToSparql)
                    .collect(Collectors.joining(" ", "  (", ")\n"))
            );
        }

        return  valuesBuffer.toString();
    }

    /**
     * Executes construct query and if it fails executes it with additional debugging information.
     * @param query
     * @param model
     * @param bindings
     * @return
     */
    public static Model execConstruct(Query query, Model model, QuerySolution bindings) {
        return execQuery(
            QueryExecution::execConstruct,
            query,
            model,
            bindings
        );
    }


    /**
     * Executes construct query and if it fails executes it with additional debugging information.
     * @param query Query to be executed.
     * @param inputModel Model that is queried.
     * @param bindings Input binding used wihin the query.
     * @param outputModel Model where the output of the query will be stored.
     * @return
     */
    public static Model execConstruct(Query query, Model inputModel, QuerySolution bindings, Model outputModel) {
        return execQuery(
            qe -> qe.execConstruct(outputModel),
            query,
            inputModel,
            bindings
        );
    }

    /**
     * Executes select query and if it fails executes it with additional debugging information.
     * @param query
     * @param model
     * @param bindings
     * @return
     */
    public static ResultSet execSelect(Query query, Model model, QuerySolution bindings) {
        return execQuery(
            QueryExecution::execSelect,
            query,
            model,
            bindings
        );
    }

    private static <T >T execQuery(QueryExecutor<T>  queryExecutor, Query query, Model model, QuerySolution bindings) {
        try {
            return execQuery(
                queryExecutor,
                QueryExecutionFactory.create(query, model, bindings),
                false);
        } catch (RuntimeException ex) {
            LOG.error("Failed execution of query [1] for binding [2], due to exception [3]. " +
                    "The query [1] will be executed again with detailed logging turned on. " +
                    "\n\t - query [1]: \"\n{}\n\"" +
                    "\n\t - binding [2]: \"\n{}\n\"" +
                    "\n\t - exception [3]: \"\n{}\n\""
                , query, bindings, getStackTrace(ex));
        }
        LOG.error("Executing query [1] again to diagnose the cause ...");
        return execQuery(
            queryExecutor,
            QueryExecutionFactory.create(query, model, bindings),
            true);
    }

    private static <T> T execQuery(QueryExecutor<T>  queryExecutor, QueryExecution execution, boolean isDebugEnabled) {

        if (isDebugEnabled) {
            execution.getContext().set(ARQ.symLogExec, Explain.InfoLevel.ALL);
        }
        return queryExecutor.execQuery(execution);
    }

    private static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private interface QueryExecutor<T> {
        T execQuery(QueryExecution execution);
    }

}
