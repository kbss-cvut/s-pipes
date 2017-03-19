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
        for(Field f: this.getClass().getDeclaredFields()) {
            Parameter p = f.getAnnotation(Parameter.class);
            if ( p == null ) {
                continue;
            }

            LOG.trace("Processing parameter {} ", f.getName());

            AnnotatedAbstractModule m = this;
            this.getPropertyValue(ResourceFactory.createProperty(p.urlPrefix()+p.name())).visitWith(new RDFVisitor() {
                @Override
                public Object visitBlank(Resource r, AnonId id) {
                    return null;
                }

                @Override
                public Object visitURI(Resource r, String uri) {
                    try {
                        f.setAccessible(true);
                        f.set(m, r);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public Object visitLiteral(Literal l) {
                    try {
                        f.setAccessible(true);
                        f.set(m, l.getValue());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });

            if (vars.containsKey(p.name())) {
                throw new RuntimeException(StringFormatter.format("Two parameters are named the same {}, except prefix", p.name()).getValue());
            } else {
                vars.put(p.name(), f);
            }
        }
    }
}
