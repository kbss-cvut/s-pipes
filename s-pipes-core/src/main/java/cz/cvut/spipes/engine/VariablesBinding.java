package cz.cvut.spipes.engine;

import cz.cvut.spipes.util.JenaUtils;
import cz.cvut.spipes.util.RDFNodeUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class VariablesBinding {

    // TODO stream variables etc.

    private static final Logger log = LoggerFactory.getLogger(VariablesBinding.class);
    private static final int MAX_TRUNCATED_VALUE_SIZE = 300;
    QuerySolutionMap binding = new QuerySolutionMap();

    public VariablesBinding() {
    }

    //TODO move to factory
    public VariablesBinding(QuerySolution querySolution) {
        querySolution.varNames().forEachRemaining(
                key -> {
                    RDFNode value = querySolution.get(key);
                    if (value == null) {
                        log.error("Ignoring variable binding with null value for the variable name \"{}\".", key);
                    } else {
                        binding.add(key, value);
                    }
                }
        );
    }

    public VariablesBinding(@NotNull String varName, @NotNull RDFNode node) {
        binding.add(varName, node);
    }

    public RDFNode getNode(@NotNull String varName) {
        return binding.get(varName);
    }

    public void add(@NotNull String varName, @NotNull RDFNode rdfNode) {
        binding.add(varName, rdfNode);
    }

    public QuerySolution asQuerySolution() {
        return binding;
    }

    public Binding asBinding() {
        if(binding == null)
            return BindingFactory.empty();
        BindingBuilder bb = BindingBuilder.create();
        binding.varNames().forEachRemaining(v -> bb.add(Var.alloc(v), binding.get(v).asNode()));
        return bb.build();
    }

    public boolean isEmpty() {
        return !binding.varNames().hasNext();
    }

    public Iterator<String> getVarNames() {
        return binding.varNames();
    }


    /**
     * Extend this binding with provided binding.
     *
     * @return Conflicting binding that was not possible to add to this binding due to inconsistency in values.
     */
    public VariablesBinding extendConsistently(VariablesBinding newVarsBinding) {
        VariablesBinding conflictingBinding = new VariablesBinding();

        newVarsBinding.getVarNames().forEachRemaining(
                v -> {
                    RDFNode oldNode = this.getNode(v);
                    RDFNode newNode = newVarsBinding.getNode(v);

                    if ((oldNode != null) && (!oldNode.equals(newNode))) {
                        conflictingBinding.add(v, newNode);
                        log.warn("Variable \"{}\" have been bind to value \"{}\", ignoring assignment to value \"{}\".", v, oldNode, newNode);
                    } else {
                        this.add(v, newNode);
                    }
                }
        );

        return conflictingBinding;

    }

    /**
     * Returns new variables binding from this variables binding restricted to listed variables.
     * @param varNames Names of variables that should be copied from this variables binding.
     * @return new variables binding
     */
    public VariablesBinding restrictTo(@NotNull List<String> varNames) {
        VariablesBinding newBinding = new VariablesBinding();
        varNames.forEach(
            v -> {
                RDFNode oldNode = this.getNode(v);

                if (oldNode != null) {
                    newBinding.add(v, oldNode);
                }
            }
        );
        return newBinding;
    }

    /**
     * Returns new variables binding from this variables binding restricted to listed variables.
     * @param varNames Names of variables that should be copied to the new binding.
     * @return new variables binding
     */
    public VariablesBinding restrictTo(@NotNull String ... varNames) {
        return restrictTo(Arrays.asList(varNames));
    }


    public static final String BASE_URI = "http://onto.fel.cvut.cz/ontologies/s-pipes/";
    public static final String QUERY_SOLUTION = BASE_URI + "query_solution";
    public static final String HAS_BINDING = BASE_URI + "has_binding";
    public static final String HAS_BOUND_VARIABLE = BASE_URI + "has_bound_variable";
    public static final String HAS_BOUND_VALUE = BASE_URI + "has_bound_value";

    private static Property p(String property) {
        return ResourceFactory.createProperty(property);
    }

    public void save(final OutputStream os, Lang lang) {
        JenaUtils.write(os, getModel(), lang);
    }

    public Model getModel() {
        final Model model = ModelFactory.createDefaultModel();

        final Resource rQuerySolution = model.createResource(QUERY_SOLUTION + "_" + new Date().getTime());
        rQuerySolution.addProperty(RDF.type, model.createResource(QUERY_SOLUTION));

        final Iterator<String> iterator = binding.varNames();
        while (iterator.hasNext()) {
            final String varName = iterator.next();

            final Resource rBinding = model.createResource(rQuerySolution.getURI() + "/" + varName);
            rQuerySolution.addProperty(p(HAS_BINDING), rBinding);

            rBinding.addProperty(p(HAS_BOUND_VARIABLE), varName);
            rBinding.addProperty(p(HAS_BOUND_VALUE), binding.get(varName));
        }
        return model;
    }

    /**
     * This method clears the current query solution and fills it with the solution read from the RDF file.
     */
    public void load(final InputStream is, final String lang) throws IOException {
        final Model model = ModelFactory.createDefaultModel();
        model.read(is, "", lang);

        final List<Resource> listQuerySolutions = model.listResourcesWithProperty(RDF.type, model.createResource(QUERY_SOLUTION)).toList();
        if (listQuerySolutions.size() != 1) {
            throw new IOException("Found " + listQuerySolutions.size() + " query solutions, but 1 was expected.");
        }

        binding.clear();

        final Resource rQuerySolution = listQuerySolutions.get(0);

        final List<Statement> listBindings = rQuerySolution.listProperties(ResourceFactory.createProperty(HAS_BINDING)).toList();
        for (final Statement s : listBindings) {
            binding.add(
                    s.getResource().getProperty(p(HAS_BOUND_VARIABLE)).getString(),
                    s.getResource().getProperty(p(HAS_BOUND_VALUE)).getObject()
            );
        }
    }

    @Override
    public String toString() {
        return binding.asMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> RDFNodeUtils.toString(e.getValue())
                ))
                .toString();
    }


    public String toTruncatedString() {
        return binding.asMap().entrySet().stream()
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), getTruncatedValue(RDFNodeUtils.toString(e.getValue())))).
            collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).toString();
    }

    private static String getTruncatedValue(@NotNull String value) {
        if (value.length() > MAX_TRUNCATED_VALUE_SIZE) {
            return "... " + value.substring(0, MAX_TRUNCATED_VALUE_SIZE).replace("\n", "\\n") + " ...";
        }
        return value;
    }
}
