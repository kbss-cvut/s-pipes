package cz.cvut.sempipes.transform;

import cz.cvut.sempipes.model.qam.Question;
import java.net.URI;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public interface Transformer {


    Question script2Form(Model script, Resource module, Resource moduleType);

    Model form2Script(Model inputScript, Question form);
}
