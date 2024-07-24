package cz.cvut.spipes.transform;

import cz.cvut.kbss.jopa.loaders.ClasspathScanner;
import cz.cvut.kbss.jopa.loaders.DefaultClasspathScanner;
import cz.cvut.spipes.constants.SM;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.model.Command;
import org.topbraid.spin.vocabulary.SP;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SPipesUtil {

    private static final Logger log = LoggerFactory.getLogger(SPipesUtil.class);

    static Set<Class> superClasses;

    /**  Contains SP ontology loaded from file "form-ontology/sp.ttl" */
    static InfModel spModel;
    static Map<Class, SpinCommandType> spinQueries;
    static Map<Resource, SpinCommandType> spinQueriesReverse;

    static {
        if(spinQueries == null){
            spinQueries = buildSpinQueryMap();
            spinQueriesReverse = new HashMap<>();
            spinQueries.entrySet().forEach(e -> spinQueriesReverse.put(e.getValue().getResource(), e.getValue()));
        }
    }

    protected static Map<Class, SpinCommandType> buildSpinQueryMap(){
        List<Class> classes = new ArrayList<>();
        Model raw = ModelFactory.createDefaultModel();
        URL modelUrl = SPipesUtil.class.getResource("/form-ontology/sp.ttl");
        try {
            raw.read(modelUrl.openStream(), null, "TTL");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        spModel = ModelFactory.createRDFSModel(raw);

        // Find command related classes

        ClasspathScanner scanner = new DefaultClasspathScanner();
        scanner.addListener(c -> {
            if(Command.class.isAssignableFrom(c) && c.isInterface())
                classes.add(c);
        });
        scanner.processClasses("org.topbraid.spin.model");
        superClasses = new HashSet<>();

        // calculate superclasses
        for(int i = 0; i < classes.size(); i ++){
            Class cls = classes.get(i);
            for(int j = 0; j < classes.size(); j ++){
                if(j == i)
                    continue;
                if(cls.isAssignableFrom(classes.get(j))) {
                    superClasses.add(cls);
                    break;
                }
            }
        }
        // remove super classes
//        classes.removeAll(superClasses);

        //fill map
        Map<Class, SpinCommandType> map = new HashMap<>();
        classes.forEach(c -> {
            List<Resource> matchingResources = getMatchingResourceClasses(c, spModel);
            if(!matchingResources.isEmpty()) {
                Resource r = matchingResources.get(0);
                if(matchingResources.size() > 1)
                    log.warn("Ambiguous mapping spin command classes to SP resources, using <%s> from ", r.getURI(), matchingResources);
                map.put(c, type(r,c));
            }
        });
        
        return map;
    }

    protected static List<Resource> getMatchingResourceClasses(Class cls, Model m) {
//        Pattern.compile("(?i)^.*#" + cls.getSimpleName());
        Pattern pattern = Pattern.compile("^.*#" + cls.getSimpleName());
        String name = "#" + cls.getName().toLowerCase();
        List<Resource> matchingResources = new ArrayList<>();
        for(Resource rCls : m.listSubjectsWithProperty(RDF.type, RDFS.Class).toList()){
            if(!rCls.isURIResource())
                continue;
            if(pattern.matcher(rCls.getURI()).matches())
                matchingResources.add(rCls);

        }
        return  matchingResources;
    }




    private static final Set<String> S_PIPES_TERMS = new HashSet<String>() {
        {
            add(SM.next.getURI());
            add(RDF.type.getURI());
        }
    };

    public static boolean isSPipesTerm(Resource term) {
        return S_PIPES_TERMS.contains(term.getURI());
    }

    public static SpinCommandType getSPinCommandType(Resource res){
        List<SpinCommandType> classes = res.listProperties(RDF.type)
                .mapWith(s -> s.getObject())
//                .filterKeep(o -> o.isURIResource())
//                .mapWith(o -> o.asResource())
                .mapWith(spinQueriesReverse::get)
                .filterKeep(c -> c != null)
                .toList();

        if(classes.isEmpty()){
            // not a spin command
            return null;
        }

        // get most specific command types
        List<SpinCommandType> mostSpecificClasses = getMostSpecificTypes(classes);

        if(mostSpecificClasses.isEmpty()) {
            log.warn("Spin command has no specific spin command type , command is assigned following types {}." +
                    "This issue could be caused by a cycle of rdfs:subClassOf axioms. ",
                    classes.stream()
                            .map(p -> p.getResource())
                            .map(r -> String.format("<%s>", r.getURI()))
                            .collect(Collectors.joining(", "))
            );

            // when there are no leaf command types use all command types
            mostSpecificClasses = classes;
        }

        // filter non leaf classes if possible
        List<SpinCommandType> mostSpecificLeafClasses = mostSpecificClasses.stream()
                .filter(p -> !superClasses.contains(p.getClazz())).collect(Collectors.toList());
        if(mostSpecificLeafClasses.isEmpty()){
            log.warn("Spin command has no leaf spin command type, command is assigned following types {}." +
                            "This issue could resolved by specifying a leaf spin command type to the command.",
                    mostSpecificClasses.stream()
                            .map(p -> p.getResource())
                            .map(r -> String.format("<%s>", r.getURI()))
                            .collect(Collectors.joining(", "))
            );
            // use mostSpecificClasses instead
            mostSpecificLeafClasses = mostSpecificClasses;
        }

        SpinCommandType type = mostSpecificClasses.get(0);
        if(mostSpecificLeafClasses.size() > 1) {
            log.warn("Ambiguous spin command is assigned multiple spin command types {}. Using sping command type <{}>",
                    mostSpecificLeafClasses.stream()
                            .map(p -> p.getResource())
                            .map(r -> String.format("<%s>", r.getURI()))
                            .collect(Collectors.joining(", ")),
                    type.getResource()
            );
        }

        return type;
    }

    /**
     * Reduce the list of pairs to contain only those pairs which reference the most specific resource type according to
     * the {@link SPipesUtil#spModel}
     *
     * @param types
     * @return a List of pairs referencing the most specific resource types according to the {@link SPipesUtil#spModel}
     */
    protected static List<SpinCommandType> getMostSpecificTypes(List<SpinCommandType> types){
        Map<Resource, SpinCommandType> map = new HashMap<>();
        types.forEach(p -> map.put(p.getResource(), p));

        Set<Resource> superTypes = map.keySet().stream()
                .flatMap(t -> t
                        .listProperties(RDFS.subClassOf)
                        .mapWith(s -> s.getObject().asResource())
                        .filterKeep(st -> !st.equals(t))
                        .toList().stream())
                .collect(Collectors.toSet());
        superTypes.forEach(st -> map.remove(st));
        return new ArrayList<>(map.values());
    }

    public static boolean isSpinQueryType(String uri){
        return isSpinCommandType(uri, SP.Query);
    }

    public static boolean isSpinUpdateType(String uri){
        return isSpinCommandType(uri, SP.Update);
    }

    public static boolean isSpinCommandType(String uri){
        return isSpinCommandType(uri, SP.Command);
    }

    protected static boolean isSpinCommandType(final String uri, Resource commandType){
        return spinQueriesReverse.keySet().stream()
                .filter(r -> r.getURI().equals(uri))
                .filter(r -> !r.listProperties(RDFS.subClassOf).mapWith(s -> s.getObject().asResource())
                        .filterKeep(st -> st.equals(commandType)).toList().isEmpty()
                ).findAny().isPresent();
    }

    public static class SpinCommandType {
        protected Resource resource;
        protected Class clazz;

        public SpinCommandType(Resource resource, Class clazz) {
            this.resource = resource;
            this.clazz = clazz;
        }

        public Resource getResource() {
            return resource;
        }

        public Class getClazz() {
            return clazz;
        }
    }

    protected static SpinCommandType type(Resource resource, Class clazz){
        return new SpinCommandType(resource, clazz);
    }
}
