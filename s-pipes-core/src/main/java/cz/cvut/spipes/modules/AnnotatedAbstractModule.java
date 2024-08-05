package cz.cvut.spipes.modules;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.cvut.spipes.modules.handlers.FieldSetter;
import cz.cvut.spipes.modules.handlers.Handler;
import cz.cvut.spipes.modules.handlers.HandlerRegistry;
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


            HandlerRegistry handlerRegistry = HandlerRegistry.getInstance();
            FieldSetter setter = new FieldSetter(f, this);
            Handler<?> handler = handlerRegistry.getHandler(f.getType(), resource, executionContext, setter);
            handler.setValueByProperty(ResourceFactory.createProperty(p.urlPrefix()+p.name()));

        }
    }
}
