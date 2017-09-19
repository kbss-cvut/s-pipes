package cz.cvut.sempipes.service;

import cz.cvut.sempipes.dao.ScriptDao;
import cz.cvut.sempipes.model.qam.Question;
import cz.cvut.sempipes.model.spipes.Module;
import cz.cvut.sempipes.model.spipes.ModuleType;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FormService {

    private static final Logger LOG = LoggerFactory.getLogger(FormService.class);

    ScriptDao scriptsDao;



    Question loadForm(String moduleUri, String contextUri) {
        Module module = scriptsDao.getModule(moduleUri, contextUri);
        ModuleType moduleType = scriptsDao.getModuleType(moduleUri, contextUri);


        Question formQ = new Question();

        formQ.setOrigin(toOrigin(module));
        formQ.setLabel("Edit + " + module.getUri() );
        Set<Question> subQuestions = new HashSet<>();


//        moduleType.getConstraints().stream().forEach(
//            c ->{
//                Question subQ = new Question();
//                subQ.setLabel(c.getUri().toString());
//                subQuestions.add(subQ);
//            }
//        );

        // order questions

        formQ.setSubQuestions(subQuestions);

        return formQ;
    }

//    void saveForm(Question question) {
//
//        Module module = new Module();
//        String type = getQuestionByOrigin(question, "rdf:type");
//        module.getTypes().add(type);
//
//        // TODO iterate over all properties
//
//        question.getSubQuestions().stream().forEach(
//            q -> {
//                module.setProperty(origin, value);
//            }
//        );
//
//        scriptsDao.saveModule(module);
//    }

    private URI toOrigin(Module module) {
        return module.getUri();
    }


}
