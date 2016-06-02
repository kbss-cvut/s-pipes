package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.system.SPINModuleRegistry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Order of queries is not enforced.   
 *
 * Created by blcha on 10.5.16.
 */
public class ApplyConstructModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ApplyConstructModule.class);

    //sml:constructQuery
    private List<Resource> constructQueries;

    //sml:replace
    private boolean isReplace;

    public ApplyConstructModule() {
        // TODO move elsewhere
        SPINModuleRegistry.get().init(); //TODO -- downloads spin from the web (should be cached instead)
    }

    public boolean isReplace() {
        return isReplace;
    }

    public void setReplace(boolean replace) {
        isReplace = replace;
    }

    public List<Resource> getConstructQueries() {
        return constructQueries;
    }

    public void setConstructQueries(List<Resource> constructQueries) {
        this.constructQueries = constructQueries;
    }

    public ExecutionContext execute(ExecutionContext context) {

        Model defaultModel = context.getDefaultModel();

        // TODO full external context support
        // set variable binding

        //QueryExecution.setInitialBinding(QuerySolution binding)
        //sparql.engine.binding.Binding

        Model mergedModel = ModelFactory.createDefaultModel();

        //      set up variable bindings
        for (Resource constructQueryRes : constructQueries) {
            Construct spinConstructRes = constructQueryRes.as(Construct.class);

            Query query = ARQFactory.get().createQuery(spinConstructRes);

            QueryExecution execution = QueryExecutionFactory.create(query, defaultModel);

            mergedModel = mergedModel.add(execution.execConstruct());
        }

        //newModel.write(System.out, FileUtils.langTurtle);

        //TODO should return only Model ???
        return ExecutionContextFactory.createContext(mergedModel);
    }


    @Override
    public void loadConfiguration(Resource moduleRes) {

        // TODO sparql expressions
        // TODO load default values from configuration

        resource = moduleRes; //TODO remove

        // TODO does not work with string query as object is not RDF resource ???
        constructQueries = moduleRes
                .listProperties(SML.constructQuery)
                .toList().stream()
                .map(st -> st.getObject().asResource())
                .collect(Collectors.toList());

        LOG.debug("Loading spin constuct queries ... " + constructQueries);

        //TODO default value must be taken from template definition
        isReplace = this.getPropertyValue(SML.replace, false);

        if (!isReplace) {//TODO
            throw new RuntimeException("Not implemented exception for isReplace = false");
        }


    }
}
