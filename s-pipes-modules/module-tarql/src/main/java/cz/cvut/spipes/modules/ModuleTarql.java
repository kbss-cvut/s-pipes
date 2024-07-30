package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.deri.tarql.tarql;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@Deprecated //TODO merge with TarqlModule functionality
@SPipesModule(label = "tarql-XXX-2", comment = "Module to convert CSV file to RDF and query it using SPRQL query. The module wraps org.deri.tarql.tarql. This module is depracated.")
public class ModuleTarql extends AbstractModule {

    private static final String TYPE_URI = KBSS_MODULE.uri + "tarql" + "-XXX-2";

    private static Property getParameter(final String name) {
        return ResourceFactory.createProperty(TYPE_URI + "/" + name);
    }

    static final Property P_TARQL_STRING = getParameter("p-tarql-string");
    @Parameter(urlPrefix = TYPE_URI + "/", name = "p-tarql-string", comment = "File with the TARQL script." )
    private String tarqlString;

    static final Property P_ONTOLOGY_IRI = getParameter("p-ontology-iri");
    @Parameter(urlPrefix = TYPE_URI + "/", name = "p-ontology-iri", comment = "Ontology IRI")
    private String ontologyIRI;

    static final Property P_INPUT_FILE = getParameter("p-input-file");
    @Parameter(urlPrefix = TYPE_URI + "/", name = "p-input-file", comment = "Input File")
    private String inputFile;

//    /**
//     * No header
//     */
//    static final Property P_NO_HEADER = getParameter("p-no-header");
//    private boolean noHeader;

    @Override
    public ExecutionContext executeSelf() {
        log.info("Running TARQL on " + inputFile);
        Model model = ModelFactory.createDefaultModel();

        try {
            final File output = File.createTempFile("output", ".nt");

            try (PrintStream s = new PrintStream(new FileOutputStream(output))) {
                final File queryFile = File.createTempFile("query", ".tarql");
                final String queryString = tarqlString.replaceAll("\\?__FN__", "\"" + ontologyIRI + "\"");
                Files.write(Paths.get(queryFile.toURI()), queryString.getBytes());

                final PrintStream origStream = System.out;
                System.setOut(s);
                tarql.main(
                        "--ntriples",
//                        noHeader ? "-H" : "",
                        queryFile.getAbsolutePath(),
                        inputFile
                );
                System.setOut(origStream);

                model.read(new FileInputStream(output), "", "N-TRIPLE");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ExecutionContextFactory.createContext(model);
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {
        inputFile = this.getStringPropertyValue(P_INPUT_FILE);
        tarqlString = this.getStringPropertyValue(P_TARQL_STRING);
        ontologyIRI = this.getStringPropertyValue(P_ONTOLOGY_IRI);
//        noHeader = this.getPropertyValue(P_NO_HEADER, false);
    }
}
