package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.VocabularyJena;
import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloneFormModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(CloneFormModule.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "clone-form";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private SparqlServiceHelper serviceHelper = new SparqlServiceHelper();

    @Parameter(urlPrefix = TYPE_PREFIX, name = "sample-form-service-url")
    private String sampleFormServiceUrl;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "data-service-url")
    private String dataServiceUrl;

    @Override
    ExecutionContext executeSelf() {

        Model sampleFormModel = serviceHelper.getModel(sampleFormServiceUrl);
        Model dataModel = serviceHelper.getModel(dataServiceUrl);


        LOG.trace("Binding root question of sample form ... ");
        getRootQuestion(sampleFormModel, false);
        LOG.trace("Binding root question of form data ... ");
        getRootQuestion(sampleFormModel, true);
        // TODO ResourceUtils.renameResource()

        return null;
    }

    private void getRootQuestion(Model formModel, boolean isNoRootAllowed) {
        List<Resource> sampleFormRootQList = formModel
            .listSubjects().filterDrop(
                subj -> formModel.listResourcesWithProperty(VocabularyJena.s_p_has_related_question, subj).hasNext()
            ).toList();

        if (!isNoRootAllowed) {
            if (sampleFormRootQList.size() == 0) {
                LOG.warn("Could not find root question of the model.");
            }
        }
        if (sampleFormRootQList.size() > 2) {
            LOG.warn("there are more than 2 root questions in the model.");
        }



            Resource sampleFormRootQ = sampleFormRootQList.get(0);
    }


    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

}
