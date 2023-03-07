package cz.cvut.spipes.debug.tree;

import java.util.List;
import java.util.stream.Collectors;

import cz.cvut.spipes.debug.model.ModuleExecution;

public class ExecutionTree {

    private ModuleExecutionNode rootNode;

    public ExecutionTree(List<ModuleExecution> executionList) {
        rootNode = buildTree(executionList);
    }

    private ModuleExecutionNode buildTree(List<ModuleExecution> executionList) {
        ModuleExecution rootExecution = executionList.stream()
                .filter(e -> e.getHas_next() == null)
                .findFirst()
                .orElse(null);

        ModuleExecutionNode rootNode = new ModuleExecutionNode(rootExecution);

        List<ModuleExecutionNode> executionNodes = executionList.stream()
                .map(ModuleExecutionNode::new)
                .collect(Collectors.toList());

        for (int i = 0; i < executionNodes.size(); i++){
            if (executionNodes.get(i).getId().equals(rootExecution.getId())){
                executionNodes.set(i, rootNode);
            }
            for (ModuleExecutionNode executionNode : executionNodes) {
                String nextId = executionNode.getExecution().getHas_next();
                String currentId = executionNodes.get(i).getId();
                if (nextId != null && nextId.equals(currentId)) {
                    executionNodes.get(i).addInputExecution(executionNode);
                }
            }
        }
        return rootNode;
    }


    public ModuleExecutionNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(ModuleExecutionNode rootNode) {
        this.rootNode = rootNode;
    }

}
