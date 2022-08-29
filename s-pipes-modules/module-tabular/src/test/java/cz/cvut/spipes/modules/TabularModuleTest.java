package cz.cvut.spipes.modules;

import cz.cvut.spipes.config.ExecutionConfig;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.exception.ResourceNotUniqueException;
import cz.cvut.spipes.modules.exception.TableSchemaException;
import cz.cvut.spipes.test.JenaTestUtils;
import cz.cvut.spipes.util.StreamResourceUtils;
import org.apache.jena.rdf.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

 class TabularModuleTest extends AbstractModuleTestHelper {

    private TabularModule module;
    private final String DATA_PREFIX = "http://onto.fel.cvut.cz/data/";

    @BeforeEach
     void setUp() {
        module = new TabularModule();

        module.setReplace(true);
        module.setDelimiter('\t');
        module.setQuoteCharacter('"');
        module.setDataPrefix(DATA_PREFIX);
        module.setOutputMode(Mode.STANDARD);

        module.setInputContext(ExecutionContextFactory.createEmptyContext());
    }

    @Test
     void executeWithSimpleTransformation() throws URISyntaxException, IOException {
        module.setSourceResource(
            StreamResourceUtils.getStreamResource(
                "http://test-file",
                getFilePath("countries.tsv"))
        );

        ExecutionContext outputContext = module.executeSelf();

        assertTrue(outputContext.getDefaultModel().size() > 0);
    }

    @Test
     void executeWithDuplicateColumnsThrowsResourceNotUniqueException()
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
     void checkDefaultConfigurationAgainstExemplaryModelOutput() throws URISyntaxException, IOException {
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


     @DisplayName("Executes Tabular module with or without csvw:propertyUrl.")
     @ParameterizedTest(name = "{index} => message=''Test {0} (csvw:propertyUrl) in the schema''")
     @ValueSource(strings = {"withProperty", "withoutProperty"})
     void executeSelfChecksSchemaWithoutProperty(String folderName) throws URISyntaxException, IOException {
        module.setSourceResource(
                StreamResourceUtils.getStreamResource(DATA_PREFIX, getFilePath("examples/" + folderName + "/input.tsv"))
        );

        Model inputModel = JenaTestUtils.laodModelFromResource("/examples/" + folderName + "/input-data-schema.ttl");
        module.setInputContext(ExecutionContextFactory.createContext(inputModel));

        ExecutionContext outputContext = module.executeSelf();
        Model actualModel = outputContext.getDefaultModel();

        Model expectedModel = ModelFactory.createDefaultModel()
                .read(getFilePath("examples/" + folderName + "/expected-output.ttl").toString());
        assertTrue(actualModel.isIsomorphicWith(expectedModel));
    }



    @DisplayName("Executes Tabular module with the different number of columns in the schema.")
    @ParameterizedTest(name = "{index} => message=''{0} in the schema''")
    @ValueSource(strings = {"moreColumns", "lessColumns", "noColumns"})
    void executeSelfThrowsException(String folderName) throws URISyntaxException, IOException {
        assumeTrue(ExecutionConfig.isExitOnError());
        module.setSourceResource(
                StreamResourceUtils.getStreamResource(DATA_PREFIX,getFilePath("examples/" + folderName + "/input.tsv"))
        );

        Model inputModel = JenaTestUtils.laodModelFromResource("/examples/" + folderName + "/input-data-schema.ttl");
        module.setInputContext(ExecutionContextFactory.createContext(inputModel));

        assertThrows(TableSchemaException.class, () -> module.executeSelf());
    }

    @Test
    void executeSelfWithDataSchemaNoHeaderReturnsNamedColumnsFromSchema()
            throws URISyntaxException, IOException {
        module.setSkipHeader(true);

        module.setSourceResource(
                StreamResourceUtils.getStreamResource(DATA_PREFIX,getFilePath("examples/noHeader/schemaExample/input.csv"))
        );

        Model inputModel = JenaTestUtils.laodModelFromResource("/examples/noHeader/schemaExample/input-data-schema.ttl");
        module.setInputContext(ExecutionContextFactory.createContext(inputModel));

        ExecutionContext outputContext = module.executeSelf();

        String[] columns = new String[]{"col_1", "col_2", "col_3", "col_4", "col_5"};


        for (int i = 2; i <= 4; i++) {
            Resource resource = ResourceFactory.createResource(DATA_PREFIX + "#row-" + i);
            for (String column: columns) {
                Property property = ResourceFactory.createProperty(DATA_PREFIX, column);
                assertTrue(outputContext.getDefaultModel().contains(resource, property));
            }
        }
    }

    @Test
    void executeSelfWithNoDataSchemaNoHeaderReturnsAutonamedColumns()
            throws URISyntaxException, IOException {
        module.setSkipHeader(true);

        module.setSourceResource(
                StreamResourceUtils
                        .getStreamResource(DATA_PREFIX,getFilePath("examples/noHeader/noSchemaExample/input.tsv"))
        );

        ExecutionContext outputContext = module.executeSelf();

        for (int i = 2; i <= 4; i++) {
            Resource resource = ResourceFactory.createResource(DATA_PREFIX + "#row-" + i);
            for (int j = 1; j <= 6; j++) {
                String columnName = "column_" + j;
                Property property = ResourceFactory.createProperty(DATA_PREFIX, columnName);

                assertTrue(outputContext.getDefaultModel().contains(resource, property));
            }
        }
    }

     @Test
     void executeSelfWithBNodesInSchema() throws IOException, URISyntaxException {
         module.setSourceResource(
                 StreamResourceUtils.getStreamResource(DATA_PREFIX, getFilePath("examples/blankNodes/input.tsv"))
         );

         Model inputModel = JenaTestUtils.laodModelFromResource("/examples/blankNodes/input-data-schema.ttl");
         module.setInputContext(ExecutionContextFactory.createContext(inputModel));

         ExecutionContext outputContext = module.executeSelf();
         Model actualModel = outputContext.getDefaultModel();

         Model expectedModel = ModelFactory.createDefaultModel()
                 .read(getFilePath("examples/blankNodes/expected-output.ttl").toString());
         assertTrue(actualModel.isIsomorphicWith(expectedModel));
     }

    @Override
    public String getModuleName() {
        return "tabular";
    }

    public Path getFilePath(String fileName) throws URISyntaxException {
        return Paths.get(getClass().getResource("/" + fileName).toURI());
    }
}