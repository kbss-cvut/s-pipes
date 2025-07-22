package cz.cvut.spipes.debug.dto;

import java.util.Date;
import java.util.List;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraint;
import cz.cvut.kbss.jopa.model.annotations.ParticipationConstraints;
import cz.cvut.spipes.Vocabulary;

@OWLClass(iri = Vocabulary.s_c_pipeline_execution)
public class PipelineExecutionDto extends ExecutionThing {

    @OWLDataProperty(iri = Vocabulary.s_p_has_execution_start_date)
    private Date has_pipepline_execution_date;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_module_execution)
    @ParticipationConstraints({
            @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_module_execution)
    })
    private List<ModuleExecutionDto> has_module_executions;

    public Date getHas_pipepline_execution_date() {
        return has_pipepline_execution_date;
    }

    private Date has_pipeline_execution_finish_date;

    public Date getHas_pipeline_execution_finish_date() {
        return has_pipeline_execution_finish_date;
    }

    public void setHas_pipeline_execution_finish_date(Date has_pipeline_execution_finish_date) {
        this.has_pipeline_execution_finish_date = has_pipeline_execution_finish_date;
    }

    public void setHas_pipepline_execution_date(Date has_pipepline_execution_date) {
        this.has_pipepline_execution_date = has_pipepline_execution_date;
    }

    private String has_pipeline_execution_status;

    public String getHas_pipeline_execution_status() {
        return has_pipeline_execution_status;
    }

    public void setHas_pipeline_execution_status(String has_pipeline_execution_status) {
        this.has_pipeline_execution_status = has_pipeline_execution_status;
    }

    private String has_script;

    public String getHas_script() {
        return has_script;
    }

    public void setHas_script(String has_script) {
        this.has_script = has_script;
    }

    private String has_executed_function;

    public String getHas_executed_function() {
        return has_executed_function;
    }

    public void setHas_executed_function(String has_executed_function) {
        this.has_executed_function = has_executed_function;
    }

    private String has_executed_function_script_path;

    public String getHas_executed_function_script_path() {
        return has_executed_function_script_path;
    }

    public void setHas_executed_function_script_path(String has_executed_function_script_path) {
        this.has_executed_function_script_path = has_executed_function_script_path;
    }

    public List<ModuleExecutionDto> getHas_module_executions() {
        return has_module_executions;
    }

    public void setHas_module_executions(List<ModuleExecutionDto> has_module_executions) {
        this.has_module_executions = has_module_executions;
    }

}

