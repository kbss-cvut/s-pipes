package cz.cvut.spipes.debug.tree;

import java.util.ArrayList;
import java.util.List;

import cz.cvut.spipes.debug.model.ModuleExecution;

public class ModuleExecutionNode {

    private ModuleExecution execution;

    private List<ModuleExecutionNode> inputExecutions = new ArrayList<>();

    public ModuleExecutionNode(ModuleExecution execution) {
        this.execution = execution;
    }

    public ModuleExecution getExecution() {
        return execution;
    }

    public void setExecution(ModuleExecution execution) {
        this.execution = execution;
    }

    public List<ModuleExecutionNode> getInputExecutions() {
        return inputExecutions;
    }

    public void setInputExecutions(List<ModuleExecutionNode> inputExecutions) {
        this.inputExecutions = inputExecutions;
    }

    public void addInputExecution(ModuleExecutionNode execution){
        this.inputExecutions.add(execution);
    }

    public String getId() {
        return execution.getId();
    }
}
