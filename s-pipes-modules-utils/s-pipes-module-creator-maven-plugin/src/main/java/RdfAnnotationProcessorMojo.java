import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

@Mojo(name = "process-annotations", defaultPhase = LifecyclePhase.COMPILE)
public class RdfAnnotationProcessorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    @Parameter(required = true, readonly = true, defaultValue = "")
    String moduleClassName;

    @Parameter(required = true, readonly = true, defaultValue = "")
    String ontologyFilename;

    private final Log log = getLog();

    private final Class<cz.cvut.spipes.modules.Parameter> paramAnnotation = cz.cvut.spipes.modules.Parameter.class;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            writeParamsToOntologies();
        } catch (Exception e) {
            log.error("Failed to execute s-pipes annotation processing: ", e);
            throw new MojoExecutionException("Exception during s-pipes execution", e);
        }
    }

    private void writeParamsToOntologies() throws MalformedURLException, ClassNotFoundException {
        final Class<?> classObject = readModuleClass(moduleClassName);
        final var serializedConstraints = readConstraintsFromClass(classObject);
        writeConstraintsToGeneratedRdf(serializedConstraints);
    }

    private Class<?> readModuleClass(String className) throws MalformedURLException, ClassNotFoundException {
        final File classesDirectory = new File(project.getBuild().getOutputDirectory());
        final URL classesUrl = classesDirectory.toURI().toURL();
        final URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classesUrl}, getClass().getClassLoader());
        final Class<?> classObject = classLoader.loadClass("cz.cvut.spipes.modules." + className);

        log.info("Loaded class " + classObject.toGenericString());
        return classObject;
    }

    private String readConstraintsFromClass(Class<?> classObject) {
        return Arrays.stream(classObject.getDeclaredFields())
                .filter((field) -> field.isAnnotationPresent(paramAnnotation))
                .map((field) -> field.getAnnotation(paramAnnotation))
                .map(this::serializeAnnotation)
                .collect(Collectors.joining("\n"));
    }

    private void writeConstraintsToGeneratedRdf(String serialized) {
        try {
            final var path = Path.of(project.getBuild().getOutputDirectory() + "/cz/cvut/spipes/modules/" + ontologyFilename);
            final var lines = Files.readAllLines(path);
            final var sb = new StringBuilder();
            for (final var line : lines) {
                sb.append(line);
                sb.append("\n");
                if (line.contains("rdf:type sm:Module ;")) {
                    sb.append("@start-params");
                }
            }
            final var out = sb.toString()
                    .replaceAll("spin:constraint \\[\\n.+\\n.+\\n.+\\n.+\\] ;", "")
                    .replace("@start-params", (serialized.length() == 0)
                            ? ""
                            : serialized + "\n");
            Files.write(path, out.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readJenaModel() {
        log.info(ontologyFilename);
        Model model = ModelFactory.createDefaultModel();
        model.read(project.getBuild().getOutputDirectory() + "/cz/cvut/spipes/modules/" + ontologyFilename);
        model.write(System.out, "TTL");
    }

    private String serializeAnnotation(cz.cvut.spipes.modules.Parameter annotation) {
        final String template = "    spin:constraint [\n" +
                "      rdf:type spl:Argument ;\n" +
                "      spl:predicate <<url>> ;\n" +
                "      rdfs:comment \"<<desc>>\" ;\n" +
                "    ] ;";
        return template.replaceAll("<<url>>", annotation.urlPrefix() + annotation.name())
                .replaceAll("<<desc>>", "Automatically generated field: " + annotation.name());
    }
}
