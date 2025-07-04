package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.AppConstants;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StreamResourceRegistry;
import cz.cvut.spipes.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.deri.tarql.tarql;
import cz.cvut.spipes.spin.model.Construct;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

// TODO merge with ModuleTarql functionality
@Slf4j
@SPipesModule(label = "tarql", comment = "\"Runs one or more TARQL Construct queries on the input triples. The output RDF " +
        "will consist of the constructed triples and (unless sml:replace is true) the input triples.\"")
public class TarqlModule extends AnnotatedAbstractModule {

    //tarql [options] query.sparql [table.csv [...]]

    private static final String TARQL_PROGRAM = AppConstants.BIN_DIR + "/tarql";

    @Parameter(iri = SML.constructQuery, comment = "The TARQL Construct queries that deliver the triples that shall be added.")
    private List<Resource> constructQueries;

    // TODO not used field
    private String tableFilePath;

    @Parameter(iri = SML.replace, comment = "If set to true, the output triples will only contain the " +
            "constructed triples. If no values or false are specified, the output will be the union of the input triples " +
            "and the constructed triples.")
    private boolean isReplace = false;

    @Parameter(iri = SML.sourceFilePath, comment = "Source CSV file.")
    private String sourceFilePath;

    @Override
    public ExecutionContext executeSelf() {

        //Model defaultInputModel = context.getDefaultModel();

        // TODO full external context support
        // set variable binding

        // TODO implement support for input graph
        //      (naive solution would be to create s,p,o columns in new CSV file),
        //      but this has problems with blank nodes

        Path p = Paths.get("/home/blcha/s-pipes-log.txt");

        Model mergedModel = ModelFactory.createDefaultModel();
        if (! isReplace) { // TODO mozno sa mozu zmenit blank nody (asi by som mal mergovat do defaultneho modelu ?)
            if (executionContext.getDefaultModel() != null) {
                mergedModel.add(executionContext.getDefaultModel());
            }
        }


        StreamResource res = StreamResourceRegistry.getInstance().getResourceByUrl(sourceFilePath);
        String tabularDataFilePath = null;
        if (res != null) {

            try {
                File tabularDataFile = File.createTempFile("output", ".tabular.txt");
                Files.write(tabularDataFile.toPath(), res.getContent());
                tabularDataFilePath = tabularDataFile.getAbsolutePath();
            } catch (IOException e) {
                throw new RuntimeException("Could not write tabular data stream to temporary file: {}", e);
            }
        } else {
            tabularDataFilePath = sourceFilePath;
        }

        log.debug("Processing tabular data from file path {}.", tabularDataFilePath);

        //      set up variable bindings
        for (Resource constructQueryRes : constructQueries) {
            Construct spinConstructRes = constructQueryRes.as(Construct.class);

            Query query = QueryUtils.createQuery(spinConstructRes);

            try {
                // save string query to temporary file
                final File queryFile = File.createTempFile("query", ".tarql");
                final String queryString = query.toString();
                //final String queryString = query.toString().replaceAll("\\?__FN__", "\"" + ontologyIRI + "\"");
                Files.writeString(queryFile.toPath(), query.toString(), Charset.defaultCharset(), StandardOpenOption.APPEND );

                // execute tarql query.sparql table.csv
                final File outputFile = File.createTempFile("output", ".ttl");

                try (PrintStream s = new PrintStream(new FileOutputStream(outputFile))) {

                    final PrintStream origStream = System.out;
                    System.setOut(s);
                    tarql.main(
//                            "--ntriples",
//                        noHeader ? "-H" : "",
                            queryFile.getAbsolutePath(),
                            tabularDataFilePath
                    );
                    System.setOut(origStream);

                    // merge output to model
                    mergedModel.read(new FileInputStream(outputFile), null, FileUtils.langTurtle);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //newModel.write(System.out, FileUtils.langTurtle);

        //TODO should return only Model ???
        ExecutionContext ec = ExecutionContextFactory.createContext(mergedModel);
        return ec;
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.uri + "tarql";
    }

    @Override
    public void loadManualConfiguration() {
        log.debug("Loaded {} spin construct queries.", constructQueries.size());
    }
}
