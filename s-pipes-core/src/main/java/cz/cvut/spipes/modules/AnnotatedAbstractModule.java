package cz.cvut.spipes.modules;

import cz.cvut.spipes.modules.handlers.*;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The `AnnotatedAbstractModule` class extends the `AbstractModule` class and provides
 * an implementation for loading the module's configuration using {@link Parameter} annotation.
 *
 * <p>Fields in subclasses of `AnnotatedAbstractModule` should be annotated with the
 * {@link Parameter} annotation to indicate that they are configurable parameters.
 * The class will automatically detect these parameters, validate them, and populate
 * them with the appropriate values during the module's initialization.
 */
public abstract class AnnotatedAbstractModule extends AbstractModule {

    private static final Logger log = LoggerFactory.getLogger(AnnotatedAbstractModule.class);

    /**
     * Loads the configuration for the module by scanning fields annotated with {@link Parameter}
     * and setting their values using the appropriate {@link Setter} and {@link Handler} implementations.
     *
     * <p>This method is responsible for processing the fields declared in the class and its superclasses,
     * that are annotated with {@link Parameter}. It ensures that each field is processed only once and
     * verifies that no two fields share the same {@link Parameter#iri()}. If a duplicate is found,
     * it throws a {@link RuntimeException}.
     *
     * <p>For each annotated field:
     * <ul>
     *   <li>A {@link Setter} is selected based on the field's type (e.g., {@link ListSetter} for lists).</li>
     *   <li>The {@link HandlerRegistry} retrieves a matching {@link Handler} for the field's type,
     *       which is responsible for setting the value of the field based on its {@link Parameter#iri()}.</li>
     * </ul>
     *
     * <p>The method continues processing all declared fields, moving up through the class hierarchy
     * until it reaches the {@code AnnotatedAbstractModule} class itself.
     *
     * <p>After all automatically configurable fields have been processed, it invokes the
     * {@link #loadManualConfiguration()} method to allow subclasses to handle any custom configuration
     * that requires manual intervention.
     *
     * @throws RuntimeException if two or more fields in the class hierarchy share the same
     *         {@link Parameter#iri()}.
     */
    @Override
    public void loadConfiguration() {

        Class<? extends AnnotatedAbstractModule> clazz = this.getClass();

        while(clazz != AnnotatedAbstractModule.class){
            final Map<String, Field> vars = new HashMap<>();
            for (final Field f : clazz.getDeclaredFields()) {
                final Parameter p = f.getAnnotation(Parameter.class);
                if (p == null) {
                    continue;
                } else if (vars.containsKey(p.iri())) {
                    throw new RuntimeException(String.format("Two parameters have same iri %s", p.iri()));
                } else {
                    vars.put(p.iri(), f);
                }

                log.trace("Processing parameter {} ", f.getName());

                Setter setter;
                if (f.getType() == List.class) {
                    setter = new ListSetter(f, this);
                } else {
                    setter = new FieldSetter(f, this);
                }
                HandlerRegistry handlerRegistry = HandlerRegistry.getInstance();
                Handler<?> handler = handlerRegistry.getHandler(f.getType(), resource, executionContext, setter);
                handler.setValueByProperty(ResourceFactory.createProperty(p.iri()));
            }
            clazz = (Class<? extends AnnotatedAbstractModule>) clazz.getSuperclass();
        }
        loadManualConfiguration();
    }

    /**
     * This abstract method is intended to be overridden by subclasses to manually load
     * specific configurations that are not automatically processed by the
     * {@link #loadConfiguration()} method.
     * <p>
     * The {@link #loadConfiguration()} method first handles automated configuration by
     * scanning annotated fields and invoking handlers to set their values. Once that
     * process is complete, {@code loadManualConfiguration} is called to allow subclasses
     * to define any additional custom configuration logic that is required.
     * <p>
     * Subclasses can choose to override this method to provide custom configurations
     * that cannot be handled automatically. If no manual configuration is necessary,
     * the default implementation can be used without changes.
     */
    public void loadManualConfiguration(){
        // Default implementation: no manual configuration
    };
}
