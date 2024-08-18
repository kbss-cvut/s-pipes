package cz.cvut.spipes.modules;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.cvut.spipes.modules.handlers.*;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The `AnnotatedAbstractModule` class extends the `AbstractModule` class and provides
 * an implementation for loading the module's configuration using {@link Parameter} annotation.
 *
 * <p>Fields in subclasses of `AnnotatedAbstractModule` should be annotated with the
 * {@link Parameter} annotation to indicate that they are configurable parameters.
 * The class will automatically detect these parameters, validate them, and populate
 * them with the appropriate values during the module's initialization.
 *
 * <p>The {@link #loadConfiguration()} method is overridden to handle this process.
 * It identifies all fields annotated with {@link Parameter}, checks for duplicate
 * parameter names, and uses the appropriate `Setter` and `Handler` implementations
 * to assign values to the fields.
 */
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

            Setter setter;
            if(f.getType() == List.class){
                setter = new ListSetter(f, this);
            }else{
                setter = new FieldSetter(f, this);
            }
            HandlerRegistry handlerRegistry = HandlerRegistry.getInstance();
            Handler<?> handler = handlerRegistry.getHandler(f.getType(), resource, executionContext, setter);
            handler.setValueByProperty(ResourceFactory.createProperty(p.urlPrefix()+p.name()));
        }
    }
}
