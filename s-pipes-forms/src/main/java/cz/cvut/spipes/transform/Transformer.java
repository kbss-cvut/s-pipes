package cz.cvut.spipes.transform;

import cz.cvut.sforms.model.Question;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.Map;

public interface Transformer {

    Question script2Form(Model script, Resource module, Resource moduleType);

    Map<String, Model> form2Script(Model inputScript, Question form, String moduleType);

    Question functionToForm(Model script, Resource function);
}
