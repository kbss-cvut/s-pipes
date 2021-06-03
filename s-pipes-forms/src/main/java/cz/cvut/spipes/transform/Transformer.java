package cz.cvut.spipes.transform;

import cz.cvut.sforms.model.Question;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelChangedListener;
import org.apache.jena.rdf.model.Resource;

import java.util.Map;

public interface Transformer {

    Question script2Form(Resource module, Resource moduleType);

    Map<String, Model> form2Script(Model inputScript, Question form, String moduleType);

    //this is possible however we do not know about changed files
    void form2ScriptNew(Model scripts, Question form, String moduleType);

    void registerListener(ModelChangedListener listener);

    Question functionToForm(Model script, Resource function);
}
