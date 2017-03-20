package cz.cvut.sempipes.modules;

import com.sun.javafx.binding.StringFormatter;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class AnnotatedAbstractModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotatedAbstractModule.class);

    @Override
    public void loadConfiguration() {
        final Map<String,Field> vars = new HashMap<>();
        for(final Field f: this.getClass().getDeclaredFields()) {
            final Parameter p = f.getAnnotation(Parameter.class);
            if ( p == null ) {
                continue;
            } else if (vars.containsKey(p.name())) {
                throw new RuntimeException(StringFormatter.format("Two parameters are named the same {}, except prefix", p.name()).getValue());
            } else {
                vars.put(p.name(), f);
            }

            LOG.trace("Processing parameter {} ", f.getName());

            RDFNode node = this.getPropertyValue(ResourceFactory.createProperty(p.urlPrefix()+p.name()));
            if ( node != null ) {
                final Object result = node.visitWith(new RDFVisitor() {
                    @Override
                    public Object visitBlank(Resource r, AnonId id) {
                        return null;
                    }

                    @Override
                    public Object visitURI(Resource r, String uri) { return r; }

                    @Override
                    public Object visitLiteral(Literal l) { return l.getValue(); }
                });
                try {
                    f.setAccessible(true);
                    f.set(this, result);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
