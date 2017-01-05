package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Miroslav Blasko on 5.1.17.
 */
public class ModuleGetDatasetDescriptorsTest {

    @Test
    @Ignore
    public void executeSelf() throws Exception {

        //TODO it should work with empty context as well
        ExecutionContext inCtx = ExecutionContextFactory.createEmptyContext();
//        ExecutionContext inCtx = ExecutionContextFactory.createContext(new VariablesBinding(
//                "p-dataset-iri",
//                ResourceFactory.createPlainLiteral("http://linked.opendata.cz/resource/dataset/vavai/tenders/vocabulary")
//        ));

        ModuleGetDatasetDescriptors m = new ModuleGetDatasetDescriptors();
        m.setpDatasetIRI("http://linked.opendata.cz/resource/dataset/vavai/tenders/vocabulary");

        m.setInputContext(inCtx);

        ExecutionContext outCtx = m.executeSelf();

        Model outModel  = outCtx.getDefaultModel();

        assertTrue(outModel.size() > 0);

    }

}