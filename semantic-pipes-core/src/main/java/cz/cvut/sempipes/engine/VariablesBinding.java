package cz.cvut.sempipes.engine;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
public class VariablesBinding {

    // TODO stream variables etc.

    private static Logger LOG = LoggerFactory.getLogger(VariablesBinding.class);
    QuerySolutionMap binding = new QuerySolutionMap();

    public VariablesBinding(){
    }

    //TODO move to factory
    public VariablesBinding(QuerySolution querySolution) {
        binding.addAll(querySolution);
    }

    public VariablesBinding(String varName, RDFNode node) {
        binding.add(varName, node);
    }

    public RDFNode getNode(String varName) {
        return binding.get(varName);
    }

    public void add(String varName, RDFNode rdfNode) {
        binding.add(varName, rdfNode);
    }

    public QuerySolution asQuerySolution() {
        return binding;
    }

    public boolean isEmpty() {
        return ! binding.varNames().hasNext();
    }

    public Iterator<String> getVarNames() {
        return binding.varNames();
    }



    /**
     * Extend this binding by provided binding.
     * @return Conflicting binding that was not possible to add to this binding due to inconsistency in values.
     */
    public VariablesBinding extendConsistently(VariablesBinding newVarsBinding) {
        VariablesBinding conflictingBinding = new VariablesBinding();

        newVarsBinding.getVarNames().forEachRemaining(
            var -> {
                RDFNode oldNode = this.getNode(var);
                RDFNode newNode = newVarsBinding.getNode(var);

                if ((oldNode != null) && (! oldNode.equals(newNode))) {
                    conflictingBinding.add(var, newNode);
                    LOG.warn("Variable \"{}\" have been bind to value \"{}\", ignoring assignment to value \"{}\".", var, oldNode, newNode);
                } else {
                    this.add(var, newNode);
                }
            }
        );

        return conflictingBinding;

    }

    @Override
    public String toString() {
        return binding.asMap().toString();
    }
}
