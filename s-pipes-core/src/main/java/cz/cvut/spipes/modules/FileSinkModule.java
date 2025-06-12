package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.util.QueryUtils;
import org.apache.jena.query.*;
import org.eclipse.rdf4j.common.io.IOUtil;
import cz.cvut.spipes.spin.model.Select;

import java.io.File;
import java.io.IOException;

@SPipesModule(label = "Persist model to files in workspace", comment = """
        Use <code>FileSinkModule</code> module to save files in a directory relative to the root script's directory. The module passes the input execution
        context downstream, i.e. to the next module in the pipeline.
        <p>
        Parameters:
        <ul>
        <li>
        <code>outputDirectory</code> - path relative to root script file, default is ".".  In an Execution contexts the\s
        root script file is the file defining the executed function or single module, see
        <code>AbstractModule.getRootScriptFile</code>.
        </li>
        <li>
        <code>selectQuery</code> - sparql select query generating files. The query must have two variables
        <ul>
        <li>
        <code>fileName</code> - should return path relative to <code>outputDirectory</code> where the file will be saved.
        </li>
        <li><code>content</code> - should return the contents of the file to saved at <code>fileName</code></li>
        </ul>
        </li>
        </ul>
        
        <b>Example of usage:</b>
        <p>
        Consider the <code>FileSinkModule</code> is used in the pipeline of a <code>hello-world</code> function defined in\s
        "<code>$SCRIPT_PATH$/</code>hello-world.sms.ttl". The module configured as shown below. Executing the function
        will create a file at "<code>$SCRIPT_PATH$/</code>target/greeting.html" with the content\s
        "<code>&lthtml&gt&ltbody&gt&lth1&gtHello world&lth1&gt&lt/body&gt&lt/html&gt</code>"
        <p>
        Configuration:
        
        <table>
        <style></style>
        <tr>
        <td><b><code>outputDirectory</code></b> = </td> <td>"target"</td>
        </tr>
        <tr>
        <td><b><code>selectQuery</code></b> = </td><td><pre>""\"SELECT ?fileName ?content{
        	BIND("greeting.html" as ?fileName)
        	BIND("&lthtml&gt&ltbody&gt&lth1&gtHello world&lth1&gt&lt/body&gt&lt/html&gt" as ?content)
        }""\"</pre></td>
        </tr>
        </table>
        """)
public class FileSinkModule extends AnnotatedAbstractModule {

    static final String FILE_NAME_VAR = "fileName"; // 1
    static final String FILE_CONTENT_VAR = "content"; // 2

    
    static final String TYPE_URI = KBSS_MODULE.uri + "file-sink";
    static final String TYPE_PREFIX = TYPE_URI + "/";
    static final String FILE_EXTENSION_PARAM = TYPE_PREFIX + "file-extension";
    static final String OUTPUT_DIRECTORY_PARAM = TYPE_PREFIX + "output-directory";
    
    @Parameter(iri = OUTPUT_DIRECTORY_PARAM)
    private String outputDirectory;

    @Parameter(iri = SML.selectQuery,
            comment = "The select query that will be used to extract file name and file content. ")
    private Select selectQuery;


    @Override
    public ExecutionContext executeSelf() {

        File root = computeOutputDirectory();
                
        Query query = QueryUtils.createQuery(selectQuery);
        QuerySolution inputBindings = executionContext.getVariablesBinding().asQuerySolution();
        try(QueryExecution execution = QueryExecutionFactory.create(query, executionContext.getDefaultModel(), inputBindings)) {
            ResultSet rs = execution.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                String fileName = qs.get(FILE_NAME_VAR).toString();
                String content = qs.get(FILE_CONTENT_VAR).toString();
                File f = new File(root, fileName);
                try {
                    IOUtil.writeString(content, f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return executionContext;
    }

    protected File computeOutputDirectory(){
        File rootFile = getRootScriptFile();

        if(outputDirectory == null)
            return rootFile.getParentFile();

        File outputDirectoryFile = new File(outputDirectory);
        return outputDirectoryFile.isAbsolute() 
                ? outputDirectoryFile
                : new File(rootFile.getParentFile(), outputDirectory);
    }
    
//    protected String getContextDirectory(){
//        SPipesScriptManager scriptManager = ScriptManagerFactory.getSingletonSPipesScriptManager();
//
//        executionContext.getVariablesBinding().getNode("_pId");
////        Binding b = BindingFactory.binding(Var.alloc("currentModule"), resource.asNode());
////        QueryExecutionFactory.create("""
////                ?currentModule sm:next :transform-data_Return
////                ?function sm:returnModule
////                """, resource.getModel()).setInitialBinding(b);
//    }


    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Select getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(Select selectQuery) {
        this.selectQuery = selectQuery;
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }
    
}
