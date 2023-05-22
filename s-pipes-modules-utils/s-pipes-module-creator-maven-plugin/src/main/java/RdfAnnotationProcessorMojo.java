import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mojo(name = "process-annotations", defaultPhase = LifecyclePhase.COMPILE)
public class RdfAnnotationProcessorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    @Parameter(defaultValue = "${project.groupId}", readonly = true, required = true)
    String javaModuleName;

    @Parameter(required = false, readonly = true)
    String moduleClassName;

    @Parameter(required = false, readonly = true)
    String modulePackageName;

    @Parameter(required = false, readonly = true)
    String ontologyFilename;

    @Parameter(required = false, readonly = true)
    GenerationMode mode;

    enum GenerationMode {
        RDF_FOR_MODULE,
        RDF_FOR_ALL_CHILDREN
    }

    private final Log log = getLog();

    private final Class<cz.cvut.spipes.modules.Parameter> PARAM_ANNOTATION = cz.cvut.spipes.modules.Parameter.class;

    private final Class<SPipesModule> MODULE_ANNOTATION = cz.cvut.spipes.modules.annotations.SPipesModule.class;


    @Override
    public void execute() throws MojoExecutionException {
        if (mode == null) {
            final var defaultMode = GenerationMode.RDF_FOR_ALL_CHILDREN;
            log.warn(String.format("No generation mode is specified, defaulting to %s. Available modes: %s",
                    defaultMode, Arrays.toString(GenerationMode.values())));
            mode = defaultMode;
        }
        log.info("Executing in the following mode: " + mode.name());

        try {
            switch (mode) {
                case RDF_FOR_MODULE:
                    generateRdfForModule();
                    return;
                case RDF_FOR_ALL_CHILDREN:
                    generateRdfForAllModules();
                    return;
                default:
                    throw new MojoExecutionException("Generation mode not specified");
            }
        } catch (RuntimeException | IOException | ReflectiveOperationException e) {
            throw new MojoExecutionException("Unexpected exception during execution", e);
        }

    }

    private void generateRdfForAllModules() throws MalformedURLException, ClassNotFoundException, FileNotFoundException {
        //read all submodules
        log.info("Generating an RDF for all sub-modules");
        List<MavenProject> submodules = project.getCollectedProjects();

        //todo create base RDF structure

        //populate RDF with module info
        for (MavenProject submodule : submodules) {
            //find module's main class
            var moduleClasses = readAllModuleClasses(submodule);
            log.info("Module: " + submodule.getName() + " | Classes: [" + moduleClasses.stream()
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", ")) + "]");

            //add module to the RDF structure
            for (Class<?> moduleClass : moduleClasses) {
                log.info("Creating RDF for module '" + moduleClass.getCanonicalName() + "'");
                final var moduleAnnotation = readModuleAnnotationFromClass(moduleClass);
                final var constraints = readConstraintsFromClass(moduleClass);
                writeConstraintsToOutputFile(constraints, moduleAnnotation);
            }

            log.info("--------------------------------------");
        }
    }

    private void generateRdfForModule() throws MojoExecutionException {
        try {
            final Class<?> classObject = readModuleClass(moduleClassName);
            final var moduleAnnotation = readModuleAnnotationFromClass(classObject);
            final var constraints = readConstraintsFromClass(classObject);
            writeConstraintsToOutputFile(constraints, moduleAnnotation);
        } catch (Exception e) {
            log.error("Failed to execute s-pipes annotation processing: ", e);
            throw new MojoExecutionException("Exception during s-pipes execution", e);
        }
    }

    private Class<?> readModuleClass(String className) throws MalformedURLException, ClassNotFoundException {
        final File classesDirectory = new File(project.getBuild().getOutputDirectory());
        final URL classesUrl = classesDirectory.toURI().toURL();
        final URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classesUrl}, getClass().getClassLoader());
        final Class<?> classObject = classLoader.loadClass(modulePackageName + "." + className); //todo fragile?

        log.info("Successfully loaded SPipes Module: " + classObject.toGenericString());
        return classObject;
    }

    private Set<Class<?>> readAllModuleClasses(MavenProject project) throws MalformedURLException, ClassNotFoundException {
        //Configure class searcher
        final File classesDirectory = new File(project.getBuild().getOutputDirectory());
        final URL classesUrl = classesDirectory.toURI().toURL();
        final URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classesUrl}, getClass().getClassLoader());
        ConfigurationBuilder reflectionConfig = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClassLoader(classLoader))
                .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner())
                .filterInputsBy(new FilterBuilder().includePackage(modulePackageName));
        Reflections reflections = new Reflections(reflectionConfig);

        //Find classes with the module annotation
        Set<Class<?>> moduleClasses = new HashSet<>();
        for (String type : reflections.getAllTypes()) {
            final Class<?> classObject = classLoader.loadClass(type);
            log.debug("Class: " + type + " | Annotations: ["
                    + Arrays.stream(classObject.getAnnotations())
                    .map(Annotation::annotationType)
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", ")) + "]");

            if (classObject.isAnnotationPresent(MODULE_ANNOTATION)) {
                moduleClasses.add(classObject);
            }
        }
        return moduleClasses;
    }

    private SPipesModule readModuleAnnotationFromClass(Class<?> classObject) {
        return classObject.getAnnotation(MODULE_ANNOTATION);
    }

    private List<cz.cvut.spipes.modules.Parameter> readConstraintsFromClass(Class<?> classObject) {
        return Arrays.stream(classObject.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(PARAM_ANNOTATION))
                .map(field -> field.getAnnotation(PARAM_ANNOTATION))
                .collect(Collectors.toUnmodifiableList());
    }

    private void writeConstraintsToOutputFile(List<cz.cvut.spipes.modules.Parameter> constraintAnnotations,
                                              SPipesModule moduleAnnotation) throws FileNotFoundException {
//        final var ontologyFolder = "/" + modulePackageName.replaceAll("[.]", "/") + "/";
//        final var ontologyFilepath = project.getBuild().getOutputDirectory() + ontologyFolder + ontologyFilename;

//        log.info("Reading ontology file: " + ontologyFilepath);
        final var model = ModelFactory.createDefaultModel();
//        model.read(ontologyFilepath);

        //region todo replace with a proper initialization
//        String prefixes = "@prefix kbss-module: <http://onto.fel.cvut.cz/ontologies/lib/module/> .\n" +
//                "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
//                "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
//                "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
//                "@prefix sm: <http://topbraid.org/sparqlmotion#> .\n" +
//                "@prefix spin: <http://spinrdf.org/spin#> .\n" +
//                "@prefix spl: <http://spinrdf.org/spl#> .\n" +
//                "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
//                "kbss-module:test-own-artifact-generated-ontology\n" +
//                "    rdf:type owl:Ontology ;\n" +
//                ".";
//        var prefixStream = new ByteArrayInputStream(prefixes.getBytes(StandardCharsets.UTF_8));
//        model.read(prefixStream, FileUtils.langTurtle);
        //endregion

        final var root = ResourceFactory.createResource();
        model.add(root, RDF.type, SM.Module);
        model.add(root, RDFS.comment, moduleAnnotation.comment());
        model.add(root, RDFS.label, moduleAnnotation.label());
        final var statements = model.listStatements(null, RDF.type, SM.Module);
        while (statements.hasNext()) {
            final var statement = statements.next();
            final var subject = statement.getSubject();
            for (var annotation : constraintAnnotations) {
                final var modelConstraint = ResourceFactory.createResource();
                model.add(modelConstraint, RDF.type, SPL.Argument);
                model.add(modelConstraint, SPL.predicate, annotation.urlPrefix() + annotation.name());
                model.add(modelConstraint, RDFS.comment, "Automatically generated field: " + annotation.name());
                model.add(subject, SPIN.constraint, modelConstraint);

                log.info("Added model constraint based on annotation: " +
                        "(name = " + annotation.name() + ", urlPrefix = " + annotation.urlPrefix() + ")");
            }
        }
//        model.write(new FileOutputStream(ontologyFilepath), FileUtils.langTurtle);
//        log.info("Successfully written constraints to the ontology file: " + ontologyFilepath);
        var out = new ByteArrayOutputStream();
        model.write(out, FileUtils.langTurtle);
        log.info("RDF for module: " + out);
    }
}
