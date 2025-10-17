package cz.cvut.spipes.util;

import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.spin.vocabulary.SP;
import cz.cvut.spipes.spin.vocabulary.SPL;
import cz.cvut.spipes.spin.vocabulary.SPR;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionEnvBase;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.vocabulary.RDF;

import java.util.*;

public class SPINUtils {
    static {
        SPipesUtil.init();
    }

    private static final Set<String> COMMON_PREFIXES = new HashSet<>() {{
        add("http://jena.hpl.hp.com/ARQ/function#");
        add(SP.NS);
        add("http://spinrdf.org/spif#");
        add("http://spinrdf.org/spin#");
        add(SPL.NS);
        add(SPR.NS);
        add(SM.uri);
        add("http://www.w3.org/2005/xpath-functions#");
    }};

    // TODO - unify api with SPipesUtil.getNonSystemFunctions
    public static List<String> getRegisteredCustomFunctions() {
        List<String> ret = new ArrayList<>();
        Iterator<String> iter = FunctionRegistry.get().keys();
        while (iter.hasNext()){
            String key = iter.next();
            if(startWithCustomPrefix(key))
                ret.add(key);
        }
        return ret;
    }

    private static boolean startWithCustomPrefix(String url) {
        return COMMON_PREFIXES.stream().noneMatch(url::startsWith);
    }

    public static boolean isExpression(RDFNode node){
        return node != null && node.isAnon() && node.asResource().hasProperty(RDF.type, SP.Expression);
    }

    /**
     * Evaluates the expression of the <code>property</code> value of the <code>resource</code> using bindings in the
     * executionContext.
     *
     * @param resource
     * @param property
     * @param executionContext
     * @return
     */
    public static RDFNode getEffectiveValue(Resource resource, Property property, ExecutionContext executionContext) {
        RDFNode valueNode = Optional.of(resource)
                .map(r -> r.getProperty(property))
                .map(Statement::getObject)
                .orElse(null);
        return SPINUtils.evaluate(valueNode, executionContext);
    }

    public static RDFNode evaluate(RDFNode constantOrExpression, ExecutionContext context) {
        return evaluate(constantOrExpression, Optional.ofNullable(context)
                .map(ExecutionContext::getVariablesBinding)
                .map(VariablesBinding::asBinding)
                .orElse(BindingFactory.empty()));
    }

    public static RDFNode evaluate(RDFNode constantOrExpression, Binding bindings) {
        RDFNode valueNode = constantOrExpression;
        if(!isExpression(valueNode))
            return valueNode;

        Model model = valueNode.getModel();
        String exprStr = valueNode.asResource().listProperties(SP.text).nextStatement().getObject().toString();

        Expr expr = ExprUtils.parse(exprStr.replaceAll("\\\\\"", "\""), model.getGraph().getPrefixMapping());

        FunctionEnv fe = new FunctionEnvBase(null, null, DatasetFactory.create(model).asDatasetGraph());
        NodeValue retNode = expr.eval(bindings, fe);

        return model.getRDFNode(retNode.asNode());
    }
}
