package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.exception.ResourceNotUniqueException;
import cz.cvut.spipes.modules.exception.TableSchemaException;
import cz.cvut.spipes.util.StreamResourceUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TabularModuleTest extends AbstractModuleTestHelper {

    private TabularModule module;
    private final String DATA_PREFIX = "http://onto.fel.cvut.cz/data/";

    @BeforeEach
    public void setUp() {
        module = new TabularModule();

        module.setReplace(true);
        module.setDelimiter('\t');
        module.setQuoteCharacter('"');
        module.setDataPrefix(DATA_PREFIX);
        module.setOutputMode(Mode.STANDARD);

        module.setInputContext(ExecutionContextFactory.createEmptyContext());
    }

    @Test
    public void executeWithSimpleTransformation() throws URISyntaxException, IOException {
        module.setSourceResource(
            StreamResourceUtils.getStreamResource(
                "http://test-file",
                getFilePath("countries.tsv"))
        );

        ExecutionContext outputContext = module.executeSelf();

        assertTrue(outputContext.getDefaultModel().size() > 0);
    }

    @Disabled
    @Test
    public void executeWithDuplicateColumnsThrowsResourceNotUniqueException()
            throws URISyntaxException, IOException {
        module.setSourceResource(StreamResourceUtils.getStreamResource(
                "http://test-file-2",
                getFilePath("duplicate_column_countries.tsv"))
        );

        ResourceNotUniqueException exception = assertThrows(
                ResourceNotUniqueException.class,
                module::executeSelf
        );

        String expectedMessage = "latitude";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void checkDefaultConfigurationAgainstExemplaryModelOutput() throws URISyntaxException, IOException {
        module.setSourceResource(
                StreamResourceUtils.getStreamResource(
                        "http://test-file",
                        getFilePath("countries.tsv"))
        );

        ExecutionContext outputContext = module.executeSelf();
        Model actualModel = outputContext.getDefaultModel();
        Model expectedModel = ModelFactory.createDefaultModel()
                .read(getFilePath("countries-model-output.ttl").toString());

        assertTrue(actualModel.isIsomorphicWith(expectedModel));
    }



    @Test
    public void testtt() throws URISyntaxException, IOException {
        module.setSourceResource(
                StreamResourceUtils.getStreamResource(DATA_PREFIX,getFilePath("examples/01/input.tsv"))
        );

        Model inputModel = JenaTestUtils.laodModelFromResource("/examples/01/input-data-schema.ttl");
        module.setInputContext(ExecutionContextFactory.createContext(inputModel));

        ExecutionContext outputContext = module.executeSelf();
        Model actualModel = outputContext.getDefaultModel();

        OutputStream out = new FileOutputStream("output-model.ttl");
        RDFDataMgr.write(out, actualModel, Lang.TURTLE);

        Model expectedModel = ModelFactory.createDefaultModel()
                .read(getFilePath("examples/01/expected-output.ttl").toString());

        assertTrue(actualModel.isIsomorphicWith(expectedModel));
    }



    @Test
    public void execute_TableSchemaWithLessColumns_throwsException() throws URISyntaxException, IOException {
        module.setSourceResource(
                StreamResourceUtils.getStreamResource(DATA_PREFIX,getFilePath("examples/02/input.tsv"))
        );

        Model inputModel = JenaTestUtils.laodModelFromResource("/examples/02/input-data-schema.ttl");
        module.setInputContext(ExecutionContextFactory.createContext(inputModel));

        assertThrows(TableSchemaException.class, () -> module.executeSelf());

    }

    @Test
    public void execute_TableSchemaWithMoreColumns_throwsException() throws URISyntaxException, IOException {
        module.setSourceResource(
                StreamResourceUtils.getStreamResource(DATA_PREFIX,getFilePath("examples/03/input.tsv"))
        );

        Model inputModel = JenaTestUtils.laodModelFromResource("/examples/03/input-data-schema.ttl");
        module.setInputContext(ExecutionContextFactory.createContext(inputModel));

        assertThrows(TableSchemaException.class, () -> module.executeSelf());
    }

    @Test
    public void execute_TableSchemaWithNoExistingColumn_throwsException() throws URISyntaxException, IOException {
        module.setSourceResource(
                StreamResourceUtils.getStreamResource(DATA_PREFIX,getFilePath("examples/04/input.tsv"))
        );

        Model inputModel = JenaTestUtils.laodModelFromResource("/examples/04/input-data-schema.ttl");
        module.setInputContext(ExecutionContextFactory.createContext(inputModel));
        assertThrows(TableSchemaException.class, () -> module.executeSelf());
    }

    @Override
    public String getModuleName() {
        return "tabular";
    }

    public Path getFilePath(String fileName) throws URISyntaxException {
        return Paths.get(getClass().getResource("/" + fileName).toURI());
    }
}