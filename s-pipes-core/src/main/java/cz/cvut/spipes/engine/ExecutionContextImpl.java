package cz.cvut.spipes.engine;

import cz.cvut.spipes.config.ContextsConfig;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class ExecutionContextImpl implements ExecutionContext {


    private Model defaultModel;
    private VariablesBinding variablesBinding;

    public Model getDefaultModel() {
        return defaultModel;
    }

    @Override
    public VariablesBinding getVariablesBinding() {
        return variablesBinding;
    }

    @Override
    public String toSimpleString() {
        return getSimpleString(false);
    }

    @Override
    public String toTruncatedSimpleString() {
        return getSimpleString(true);
    }

    public void setDefaultModel(Model defaultModel) {
        this.defaultModel = defaultModel;
    }

    public void setVariablesBinding(VariablesBinding variablesBinding) {
        this.variablesBinding = variablesBinding;
    }

    private String getSimpleString(boolean truncate) {

        String sb = "Context " + this.hashCode() + "[ \n" +
            "\t varBindings = " + getVariablesBindingString(truncate) + "\n" +
            "\t modelSize = " + defaultModel.size() +
            "]";

        return sb;
    }

    private String getVariablesBindingString(boolean truncate) {
        if (truncate) {
            return variablesBinding.toTruncatedString();
        }
        return variablesBinding.toString();
    }

    @Override
    public String getValue(String var){
        return Optional.ofNullable(getVariablesBinding().getNode(var))
                .map(RDFNode::toString)
                .orElse(null);
    }

    @Override
    public String getId() {
        return getValue(ID_PARAM);
    }

    @Override
    public String getScriptUri() {
        return getValue(P_SCRIPT_URI);
    }

    @Override
    public void setScriptUri(String scriptUri) {
        getVariablesBinding().add(
                ExecutionContext.P_SCRIPT_URI,
                getDefaultModel().createResource(scriptUri)
        );
    }


    /**
     * Get the file corresponding to the value returned by <code>{@link #getScriptUri()}</code>
     * @see ExecutionContextImpl#getScriptFiles(String, List) how scriptUri mapped to a file
     * @return
     */
    @Override
    public File getScriptFile(){
        List<File> files = getScriptFiles(getScriptUri(), ContextsConfig.getScriptPaths().stream().map(Path::toString).toList());
        if(files.isEmpty())
            throw new IllegalStateException("Cannot find script file module with id=%s and scriptUri=<%s>.".formatted(getId(), getScriptUri()));
        if(files.size() > 1 )
            throw new IllegalStateException("There are multiple script files found for module with id %s and scriptUri=<%s>.".formatted(getId(), getScriptUri()));

        return files.get(0);
    }


    /**
     * Returns a list of files within one or more locations with prefix in scriptPaths corresponding to the <code>uriStr</code>.
     * Searching for files associated with <code>uriStr</code> is done in the following order:
     *
     * <ol>
     * <li/> <code>uriStr</code> is in LocationMapper of OntDocumentManager - found mapped file is returned if exists in the file system.
     * <li/> <code>uriStr</code> is an absolute file uri - return if it exists in the file system.
     * <li/> <code>uriStr</code> is a relative path - return files with <code>path = root + uriStr</code>
     * where <code>root</code> is in <code>scriptPaths</code> and the file exists in the file system.
     * </ol>
     *
     * @param uriStr ontology uri, file uri or relative path
     * @param scriptPaths root paths to search for relative paths in. This is typically ContextsConfig.getScriptPaths()
     * @return List of files mapped to the uriStr
     */
    public static List<File> getScriptFiles(String uriStr, List<String> scriptPaths) {
        //1. Handle uriStr is an ontology iri
        // TODO - make it work with prefixed uris

        String path = OntDocumentManager.getInstance().doAltURLMapping(uriStr);
        File f = new File(path);
        if (f.exists())
            return Arrays.asList(f);

        //2. handle case where uriStr is a file uri
        URI uri = URI.create(path);

        if(uri.getScheme() != null && !uri.getScheme().equalsIgnoreCase("file") )
            return Collections.EMPTY_LIST;


        if(uri.getScheme() == null) {
            if(!uriStr.startsWith("[^/]") && !uriStr.matches("^[a-zA-Z]+:.*$")) {
                // search if relative uriStr is in any of the scriptPaths,
                // relative uri is one without a scheme starting with "./", "../" or path component.
                return scriptPaths.stream()
                        .distinct()
                        .map(p -> cannonicalFile(p, uriStr))
                        .distinct()
                        .filter(File::exists).toList();
            }

            // absolute path
            return Stream.of(new File(uriStr)).filter(File::exists).toList();
        }


        return Stream.of(new File(uri)).filter(File::exists).toList();
    }

    private static File cannonicalFile(String root, String relPath){
        String normalizedRoot = root.replaceFirst("([^/\\\\])$", "$1/");
        try{
            return new File(normalizedRoot, relPath).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
