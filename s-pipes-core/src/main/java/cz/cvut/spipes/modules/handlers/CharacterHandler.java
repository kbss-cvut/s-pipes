package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class CharacterHandler extends BaseRDFNodeHandler<Character>  {

    public CharacterHandler(Resource resource, ExecutionContext executionContext, Setter<? super Character> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    Character getRDFNodeValue(RDFNode node) {
        return node.asLiteral().getChar();
    }
}
