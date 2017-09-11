package cz.cvut.sempipes.util;


import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

public class QueryUtils {


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
                getResultRow(querySolution).stream()
                    .map(r -> serializeToSparql(r))
                    .collect(Collectors.joining(" ", "  (", ")\n"))
            );
        }

        return  valuesBuffer.toString();
    }

    private static List<RDFNode> getResultRow(QuerySolution querySolution) {
        List<RDFNode> row = new LinkedList<>();
        querySolution.varNames().forEachRemaining(
            s -> row.add(querySolution.get(s))
        );
        return row;
    }

}
