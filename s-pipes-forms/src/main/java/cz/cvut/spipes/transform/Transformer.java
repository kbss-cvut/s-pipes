package cz.cvut.spipes.transform;

import cz.cvut.sforms.model.Question;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.Map;

/**
 * Interface for transforming between SPipes module configurations and SForms model forms.
 * <p>
 *  SPipes modules are configured using RDF in SPipes scripts. Some parts of the module's configuration
 * can be scattered across multiple RDF graphs (i.e. ontologies).
 * </p>
 * <p>
 * The SForms model is used for rendering the module's configuration form in the UI.
 * </p>
 */
public interface Transformer {
    /**
     * Converts SPipes module configuration into a form.
     * SPipes module is represented as an RDF resource within an RDF model.
     * The form is represented as a tree of {@link Question} objects compliant with SForms model.
     * <p>
     * The resulting {@link Question} form contains metadata, properties, and answers extracted from the RDF model,
     * organized for UI rendering or further processing.
     * </p>
     *
     * @param module     the RDF resource representing the module instance
     * @param moduleType the RDF resource representing the module type of the module instance
     * @return a root {@link Question} representing the form for the module
     * @throws IllegalArgumentException if the URIs are not absolute
     */
    Question script2Form(Resource module, Resource moduleType);

    /**
     * Transforms a filled form back into one or more RDF models, updating or creating RDF statements
     * based on the user's answers in the form.
     * <p>
     * Handles both existing and new modules, and returns a map from ontology URIs to their updated RDF models.
     * </p>
     *
     * @param inputScript the input RDF model containing the module
     * @param form        the filled {@link Question} form
     * @param moduleType  the URI string of the module type
     * @return a map from ontology URIs to updated RDF {@link Model}s
     */
    Map<String, Model> form2Script(Model inputScript, Question form, String moduleType);

    /**
     * Generates a form for invoking a SPipes function described in an RDF model.
     * <p>
     * The form contains questions for the function URI and its parameters, allowing users
     * to provide input for a function call. The structure is based on constraints and metadata found in the
     * RDF resource of the function.
     * </p>
     *
     * @param script   the RDF model containing the function definition
     * @param function the RDF resource representing the function
     * @return a root {@link Question} representing the function call form
     * @throws IllegalArgumentException if the function URI is not absolute
     */
    Question functionToForm(Model script, Resource function);
}
