package cz.cvut.spipes.modules.handlers;


import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.*;
import java.net.MalformedURLException;
import java.net.URL;

public class URLHandler extends Handler<URL>{
    public URLHandler(Resource resource, ExecutionContext executionContext, Setter<? super URL> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        URL url;
        RDFNode urlNode = getEffectiveValue(property);

        if (urlNode != null) {
            try {
                url = new URL(urlNode.toString());
                setter.addValue(url);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
