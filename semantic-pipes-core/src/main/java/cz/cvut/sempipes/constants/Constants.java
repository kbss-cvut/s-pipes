package cz.cvut.sempipes.constants;

/**
 * Created by Miroslav Blasko on 13.5.16.
 */
public interface Constants {

    // namespaces
    public static final String NAMESPACE_SML = "http://topbraid.org/sparqlmotionlib#";
    public static final String NAMESPACE_SM = "http://topbraid.org/sparqlmotion#";
    public static final String NAMESPACE_SMF = "http://topbraid.org/sparqlmotionfunctions#";
    public static final String NAMESPACE_SPL = "http://spinrdf.org/spl#";
    public static final String NAMESPACE_KBSS_MODULE  = "http://onto.fel.cvut.cz/ontologies/lib/module/";

    // modules
    public static final String KBSS_MODULE_TARQL = NAMESPACE_KBSS_MODULE + "tarql";
    public static final String KBSS_MODULE_FORM_GENERATOR = NAMESPACE_KBSS_MODULE + "form-generator";
    public static final String SML_APPLY_CONSTRUCT = NAMESPACE_SML + "ApplyConstruct";
    public static final String SML_EXPORT_TO_RDF_FILE = NAMESPACE_SML + "ExportToRDFFile";
    public static final String SML_IMPORT_FILE_FROM_URL = NAMESPACE_SML + "ImportFileFromURL";
    public static final String SML_BIND_WITH_CONSTANT = NAMESPACE_SML + "BindWithConstant";
    public static final String SML_BIND_BY_SELECT = NAMESPACE_SML + "BindBySelect";
    public static final String SML_MERGE = NAMESPACE_SML + "Merge";
    public static final String SML_RETURN_RDF = NAMESPACE_SML + "ReturnRDF";

    // other classes
    public static final String SPL_ARGUMENT = NAMESPACE_SPL + "Argument";

    // module properties
    public static final String SML_REPLACE = NAMESPACE_SML + "replace";
    public static final String SML_CONSTRUCT_QUERY = NAMESPACE_SML + "constructQuery";
    public static final String SML_VALUE = NAMESPACE_SML + "value";
    public static final String SML_SELECT_QUERY = NAMESPACE_SML + "selectQuery";
    public static final String SML_SOURCE_FILE_PATH = NAMESPACE_SML + "sourceFilePath";
    public static final String SML_URL = NAMESPACE_SML + "url";
    public static final String SML_BASE_URI = NAMESPACE_SML + "baseURI";
    public static final String SML_SERIALIZATION = NAMESPACE_SML + "serialization";
    public static final String SML_IGNORE_IMPORTS = NAMESPACE_SML + "ignoreImports";
    public static final String SM_NEXT = NAMESPACE_SM + "next";
    public static final String SM_OUTPUT_VARIABLE = NAMESPACE_SM + "outputVariable";
    public static final String SM_RETURN_MODULE = NAMESPACE_SM + "returnModule";
    public static final String SM_FUNCTION = NAMESPACE_SM + "Function";

    // values
    public static final String SML_JSONLD = NAMESPACE_SML + "JSONLD";




}
