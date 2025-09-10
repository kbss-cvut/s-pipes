package cz.cvut.spipes.util;

import cz.cvut.spipes.spin.vocabulary.SP;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;

import java.nio.file.Path;
import java.util.*;

public class SPipesUtil {
    private static final Logger log = LoggerFactory.getLogger(SPipesUtil.class);

    // using org.topbraid:shacl  resource
    public static final String SHACL_TTL_RESOURCE = "/rdf/shacl.ttl";

    private final static Set<String> systemFunctions = Collections.synchronizedSet(new HashSet<>());
    private final static Set<String> jenaFunctions = Collections.synchronizedSet(new HashSet<>());
    private final static Set<String> shaclFunctions = Collections.synchronizedSet(new HashSet<>());
    private final static Map<String, List<String>> functionMap = Collections.synchronizedMap(new HashMap<>());

    static {
        SPARQLSubstitutions.useGraphPrefixes = true;
        loadSystemFunctions();
    }

    private static Set<String> loadSystemFunctions(){
        FunctionRegistry.init();
        jenaFunctions.addAll(getAllRegisteredFunctions());
        registerShaclFunctions();
        Set<String> systemFunctions = new HashSet<>(getAllRegisteredFunctions());
        SPipesUtil.systemFunctions.addAll(systemFunctions);
        systemFunctions.removeAll(jenaFunctions);
        shaclFunctions.addAll(systemFunctions);
        return systemFunctions;
    }

    private static void registerShaclFunctions(){
        Model shaclModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        shaclModel.read(SPipesUtil.class.getResourceAsStream(SHACL_TTL_RESOURCE), null, "ttl");
        SHACLFunctions.registerGlobalFunctions(shaclModel);
    }

    public static void init(){
        SP.getURI(); // load jena enh nodes (e.g. Construct and Select)
    }

    public static List<String> getAllRegisteredFunctions(){
        List<String> ret = new ArrayList<>();
        FunctionRegistry.get().keys().forEachRemaining(ret::add);
        return ret;
    }

    public static List<String> getNonSystemFunctions() {
        Iterator<String> iter = FunctionRegistry.get().keys();

        List<String> ret = new ArrayList<>();
        while(iter.hasNext()){
            String key = iter.next();
            if(!systemFunctions.contains(key))
                ret.add(key);
        }
        return ret;
    }

    /**
     * Reset only the functions declared in <code>model</code>. Only shacl rdf functions are loaded.
     * @param model
     */
    public static void resetFunctions(Model model){
        unregisterNonSystemFunctions();
        SHACLFunctions.registerFunctions(model);
    }

    /**
     * Reset registered functions to system functions (only jena and shacl functions).
     */
    public static void resetFunctions(){
        unregisterNonSystemFunctions();
        functionMap.clear();
    }

    /**
     * Reset functions from updated files, remove functions from removed files.
     * The reset algorithm first unregisters all functions from deleted and updated files and then registers the
     * functions from the updated models.
     *
     * If a model fails to register the functions the previously cached functions are restored.
     *
     * Behavior is undefined if there are two different shacl function definitions in two different files with the same IRI.
     *
     * @param allChangedFiles
     * @param removed
     */
    public static void resetFunctions(Map<String, Model> allChangedFiles, Set<Path> removed){
        for(Path removedFile :removed)
            unregisterNonSystemFunctions(removedFile.toString());
        List<BatchResetPart> batchResetParts = allChangedFiles.entrySet().stream().map(e -> new BatchResetPart(e.getKey(), e.getValue())).toList();
        batchResetParts.forEach(BatchResetPart::unregisterFunctions);
        batchResetParts.forEach(BatchResetPart::registerFunctions);
    }

    private static void addBackupedFunctions(Map<String, FunctionFactory> backupFunctions){
        for(Map.Entry<String, FunctionFactory> e : backupFunctions.entrySet()){
            if(FunctionRegistry.get().isRegistered(e.getKey()))
                continue;
            FunctionRegistry.get().put(e.getKey(), e.getValue());
        }
    }

    private static Map<String, FunctionFactory> unregisterNonSystemFunctions(String file){
        Map<String, FunctionFactory> _functionMap = new HashMap<>();

        List<String> functions = functionMap.remove(file);
        if(functions == null)
            return _functionMap;

        for(String key: functions) {
            FunctionFactory ff = FunctionRegistry.get().remove(key);
            _functionMap.put(key, ff);
        }
        return _functionMap;
    }

    private static void unregisterNonSystemFunctions(){
        for(String key: getNonSystemFunctions())
            FunctionRegistry.get().remove(key);
    }

    private static class BatchResetPart{
        protected String file;
        protected Model model;
        protected Map<String, FunctionFactory> backupOldModelFunctions;

        public BatchResetPart(String file, Model model) {
            this.file = file;
            this.model = model;
        }

        public void unregisterFunctions() {
            backupOldModelFunctions = unregisterNonSystemFunctions(file);
        }

        public void registerFunctions() {
            // workaround to find functions added from model
            // 1. remove all non-system functions (jena and shacl functions)
            // 2. functions from model
            // 3. add entry (file, non-system functions from Function registry) to functionMap
            // 4. return all non-system and not model functions from step 1.

            // 1.
            Map<String, FunctionFactory> backupFunctions = new HashMap<>();

            for(String fKey : getNonSystemFunctions())
                backupFunctions.put(fKey, FunctionRegistry.get().remove(fKey));

            // 2.
            try {
                SHACLFunctions.registerFunctions(model);
            }catch (Exception e){
                log.warn("Failed to reload/load functions from file \"{}\". Resetting old functions." , file, e);
                addBackupedFunctions(backupOldModelFunctions);
                addBackupedFunctions(backupFunctions);
                return;
            }
            // 3.
            List<String> modelFunctions = new ArrayList<>();
            for(String fKey : getNonSystemFunctions())
                modelFunctions.add(fKey);

            // 4.
            functionMap.put(file, modelFunctions);

            // 5.
            addBackupedFunctions(backupFunctions);
        }
    }
}
