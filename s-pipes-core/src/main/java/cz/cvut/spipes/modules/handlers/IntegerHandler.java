package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class IntegerHandler extends BaseRDFNodeHandler<Integer>{

    public IntegerHandler(Resource resource, ExecutionContext executionContext, Setter<? super Integer> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    Integer getRDFNodeValue(RDFNode node) {
        Literal literal = node.asLiteral();
        if (node.asLiteral().getValue() instanceof Integer) {
            return literal.getInt();
        } else {
            return (int) literal.getChar();
        }
    }
}
