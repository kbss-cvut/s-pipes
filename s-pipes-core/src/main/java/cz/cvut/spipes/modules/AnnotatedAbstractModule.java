package cz.cvut.spipes.modules;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFVisitor;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AnnotatedAbstractModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedAbstractModule.class);

    @Override
    public void loadConfiguration() {
        final Map<String,Field> vars = new HashMap<>();
        for(final Field f: this.getClass().getDeclaredFields()) {
            final Parameter p = f.getAnnotation(Parameter.class);
            if ( p == null ) {
                continue;
            } else if (vars.containsKey(p.name())) {
                throw new RuntimeException(String.format("Two parameters are named the same %s, except prefix", p.name()));
            } else {
                vars.put(p.name(), f);
            }

            log.trace("Processing parameter {} ", f.getName());

            RDFNode node = this.getEffectiveValue(ResourceFactory.createProperty(p.urlPrefix()+p.name()));

            if ( node != null ) {
                Object result;
                if(f.getType() == RDFNode.class) {
                    result = node;
                }else {
                    result = node.visitWith(new RDFVisitor() {
                        @Override
                        public Object visitBlank(Resource r, AnonId id) {
                            return null;
                        }

                        @Override
                        public Object visitURI(Resource r, String uri) {
                            return r;
                        }

                        @Override
                        public Object visitLiteral(Literal l) {
                            return l.getValue();
                        }
                    });
                }
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
