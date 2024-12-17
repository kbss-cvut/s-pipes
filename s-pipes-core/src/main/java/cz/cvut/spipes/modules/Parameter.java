package cz.cvut.spipes.modules;

import cz.cvut.spipes.modules.handlers.DefaultHandler;
import cz.cvut.spipes.modules.handlers.Handler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields in a module class for automatic configuration.
 * Fields annotated with {@code @Parameter} are processed during the module's configuration
 * phase to set their values using appropriate {@link Handler} implementations.
 *
 * <p>This annotation associates a field with a property in an RDF triple, where the
 * {@link #iri()} attribute specifies the IRI of the RDF property. The field's value is
 * set based on the object of the RDF triple with the given property IRI.</p>
 *
 * <p>Key attributes:
 * <ul>
 *   <li>{@link #iri()} specifies the IRI of the RDF property associated with anotated Java field.</li>
 *   <li>{@link #comment()} specifies an optional description about the parameter for documentation or informational purposes.</li>
 *   <li>{@link #handler()} allows the specification of a custom {@link Handler} implementation
 *       for managing the field's value. If not provided, the {@link DefaultHandler} is used by default.</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * @Parameter(
 *     iri = "http://example.org/property/name",
 *     comment = "The name property in the RDF data",
 *     handler = CustomHandler.class
 * )
 * private String name;
 * }
 * </pre>
 *
 * @see Handler
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {

    /**
     * Specifies the IRI of the RDF property associated with the field.
     * The IRI is used to identify the property in the RDF triple whose object will be used to set the field's value.
     *
     * @return the IRI of the RDF property
     */
    String iri();

    /**
     * An optional comment or description for the parameter.
     * This can be used for documentation or explanatory purposes.
     *
     * @return the comment associated with the parameter
     */
    String comment() default "";

    /**
     * Specifies the custom {@link Handler} implementation to use for this parameter.
     * If no custom handler is provided, the {@link DefaultHandler} is used.
     *
     * @return the class of the custom handler
     */
    Class<? extends Handler> handler() default DefaultHandler.class;
}
