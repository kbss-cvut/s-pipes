package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.util.SPINExpressions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class URLHandler extends Handler<URL>{
    public URLHandler(Resource resource, ExecutionContext executionContext, Setter<? super URL> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        URL url;
        RDFNode urlNode = getRDFNodeByProperty(property);
        if (urlNode != null) {
            try {
                url = new URL(urlNode.asLiteral().toString());
                setter.addValue(url);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
