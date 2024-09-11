package cz.cvut.spipes.modules.handlers;


import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import java.net.MalformedURLException;
import java.net.URL;

public class URLHandler extends BaseRDFNodeHandler<URL>{
    public URLHandler(Resource resource, ExecutionContext executionContext, Setter<? super URL> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    URL getRDFNodeValue(RDFNode node) throws MalformedURLException {
        return new URL(node.toString());
    }

}
