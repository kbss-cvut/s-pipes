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
        Use <code>FileSinkModule</code> module to save files in a directory relative to the script's directory. The module passes the input execution
        context downstream, i.e. to the next module in the pipeline.
        <p>
        <b>Example of usage:</b>
        <p>
        Consider an extension of the `hello world` pipeline defined in
        "<code>$SCRIPT_PATH$/</code>hello-world.sms.ttl" where the <code>FileSinkModule</code> is inserted between
        modules <code>construct-greeting</code> and <code>express-greeting_Return</code> with configuration as shown below.
        When executed, the `execute-greeting` function would create a file at
        "<code>$SCRIPT_PATH$/</code>target/greeting.html" with the content
        "<code>&lthtml&gt&ltbody&gt&lth1&gtHello world&lth1&gt&lt/body&gt&lt/html&gt</code>"
        <p>
        FileSinkModule Configuration:
        
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
    
    @Parameter(iri = OUTPUT_DIRECTORY_PARAM, comment = """
            directory where output files will be stored. Default value is ".".
            `output-directory` can be set to:
            <ul>
            <li>absolute path - files are stored relative to value of `${output-directory}`
            <li>relative path - files are stored relative to value `${scriptDirectory}/${output-directory}`</li>
            <ul>
            <br/><br/>
            `scriptDirectory` is the containing directory of the script file, i.e. the file defining the executed
            function or single module identified by the value of the `_pId` variable in the variable bindings in the
            execution contexts.
            """)
    private String outputDirectory;

    @Parameter(iri = SML.selectQuery,
            comment = """
The select query to retrieve file names with their related file contents. The query must return at least 2 columns:
- `fileName` - containing paths relative to `outputDirectory` where the files will be saved,
- `content` - the content of the file to be saved at `fileName`.
            """)
    private Select selectQuery;


    @Override
    public ExecutionContext executeSelf() {

        File scriptDir = computeOutputDirectory();
                
        Query query = QueryUtils.createQuery(selectQuery);
        QuerySolution inputBindings = executionContext.getVariablesBinding().asQuerySolution();
        try(QueryExecution execution = QueryExecutionFactory.create(query, executionContext.getDefaultModel(), inputBindings)) {
            ResultSet rs = execution.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                String fileName = qs.get(FILE_NAME_VAR).toString();
                String content = qs.get(FILE_CONTENT_VAR).toString();
                File f = new File(scriptDir, fileName);
                try {
                    IOUtil.writeString(content, f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return executionContext;
    }

    /**
     * Computes the output directory where output files will be saved. The return directory is derived from values of:
     * <ul>
     * <li><code>outputDirectory</code> - input parameter and</li>
     * <li><code>scriptDirectory</code> - derived as the parent directory of the script file containing the function/module identified by URI bound to <code>P_ID</code> in the variable
     * bindings of the execution context.</li>
     * </ul>
     *
     * @return <ul>
     *     <li><code>scriptDirectory</code> - if <code>outputDirectory</code> is not specified, i.e. <code>outputDirectory=null</code></li>
     *     <li><code>outputDirectoryFile</code> - if <code>outputDirectory</code> is an absolute path</li>
     *     <li><code>scriptDirectory/outputDirectory</code> - if <code>outputDirectory</code> is a relative path.</li>
     * </ul>
     */
    protected File computeOutputDirectory(){
        File scriptFile = executionContext.getScriptFile();

        if(outputDirectory == null)
            return scriptFile.getParentFile();

        File outputDirectoryFile = new File(outputDirectory);
        return outputDirectoryFile.isAbsolute() 
                ? outputDirectoryFile
                : new File(scriptFile.getParentFile(), outputDirectory);
    }


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
