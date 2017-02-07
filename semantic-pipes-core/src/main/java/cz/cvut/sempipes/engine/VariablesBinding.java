package cz.cvut.sempipes.engine;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
public class VariablesBinding {

    // TODO stream variables etc.

    private static Logger LOG = LoggerFactory.getLogger(VariablesBinding.class);
    QuerySolutionMap binding = new QuerySolutionMap();

    public VariablesBinding() {
    }

    //TODO move to factory
    public VariablesBinding(QuerySolution querySolution) {
        querySolution.varNames().forEachRemaining(
                key -> {
                    RDFNode value = querySolution.get(key);
                    if (value == null) {
                        LOG.error("Ignoring variable binding with null value for the variable name \"{}\".", key);
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

    public boolean isEmpty() {
        return !binding.varNames().hasNext();
    }

    public Iterator<String> getVarNames() {
        return binding.varNames();
    }


    /**
     * Extend this binding by provided binding.
     *
     * @return Conflicting binding that was not possible to add to this binding due to inconsistency in values.
     */
    public VariablesBinding extendConsistently(VariablesBinding newVarsBinding) {
        VariablesBinding conflictingBinding = new VariablesBinding();

        newVarsBinding.getVarNames().forEachRemaining(
                var -> {
                    RDFNode oldNode = this.getNode(var);
                    RDFNode newNode = newVarsBinding.getNode(var);

                    if ((oldNode != null) && (!oldNode.equals(newNode))) {
                        conflictingBinding.add(var, newNode);
                        LOG.warn("Variable \"{}\" have been bind to value \"{}\", ignoring assignment to value \"{}\".", var, oldNode, newNode);
                    } else {
                        this.add(var, newNode);
                    }
                }
        );

        return conflictingBinding;

    }

    final String BASE_URI = "http://onto.fel.cvut.cz/ontologies/semantic-pipes/";
    final String QUERY_SOLUTION = BASE_URI + "query_solution";
    final String HAS_BINDING = BASE_URI + "has_binding";
    final String HAS_BOUND_VARIABLE = BASE_URI + "has_bound_variable";
    final String HAS_BOUND_VALUE = BASE_URI + "has_bound_value";

    private static Property p(String property) {
        return ResourceFactory.createProperty(property);
    }

    public void save(final OutputStream os, final String lang) throws IOException {
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

        model.write(os, lang);
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
        return binding.asMap().toString();
    }
}
