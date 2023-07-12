package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.manager.OntoDocManager;
import cz.cvut.spipes.manager.OntologyDocumentManager;
import org.apache.jena.util.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

class RetrievePrefixesModuleTest {

    @Disabled
    @Test
    void testGetPrefixesFromFile() {
        ExecutionContext inputExecutionContext = ExecutionContextFactory.createEmptyContext();
        OntologyDocumentManager ontDocManager = OntoDocManager.getInstance();
        Path ontologiesDir = Paths.get("/home/blcha/projects/kbss/git/s-pipes/s-pipes-core/src/test/resources/manager");
        ontDocManager.registerDocuments(ontologiesDir);

        RetrievePrefixesModule retrievePrefixesModule = new RetrievePrefixesModule();
        retrievePrefixesModule.setInputContext(inputExecutionContext);
        ExecutionContext outputExecutionContext = retrievePrefixesModule.executeSelf();

        outputExecutionContext.getDefaultModel().write(System.out, FileUtils.langTurtle, null);
    }
}