package cz.cvut.sempipes.engine;

import cz.cvut.sempipes.modules.BindWithConstantModule;
import cz.cvut.sempipes.modules.Module;
import cz.cvut.sempipes.modules.TarqlModule;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.pfunction.library.concat;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.apache.jena.vocabulary.RDFSyntax.RDF;

/**
 * Created by blcha on 6.5.16.
 */
public class ExecutionEngineTest {



    @Test
    public void executeModule() throws Exception {

        // builder

        RDFNode rdfNode = null; // sparql expression
        BindWithConstantModule bindRepo = new BindWithConstantModule();
       // bindRepo.setOutputVariable("repoUrl");
        //bindRepo.setValue(rdfNode);

        Module tarqlModule = new TarqlModule();

        List<Module> inputModules = new LinkedList<>();
        inputModules.add(bindRepo);
        tarqlModule.setInputModules(inputModules);




    }



}