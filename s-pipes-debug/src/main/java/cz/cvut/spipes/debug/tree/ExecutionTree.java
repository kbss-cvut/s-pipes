package cz.cvut.spipes.debug.tree;

import java.util.ArrayList;
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

    public List<ModuleExecution> findEarliest(List<String> moduleExecutionIris) {
        List<ModuleExecution> earliestExecutions = new ArrayList<>();
        findEarliestRecursive(rootNode, moduleExecutionIris, earliestExecutions);
        return earliestExecutions;
    }

    private boolean findEarliestRecursive(ModuleExecutionNode currentNode, List<String> targetIds, List<ModuleExecution> earliestExecutions) {
        boolean foundTargetId = false;

        if (targetIds.contains(currentNode.getId())) {
            earliestExecutions.add(currentNode.getExecution());
            foundTargetId = true;
        }

        for (ModuleExecutionNode childNode : currentNode.getInputExecutions()) {
            boolean childFound = findEarliestRecursive(childNode, targetIds, earliestExecutions);
            foundTargetId = foundTargetId || childFound;
        }
        if (foundTargetId && earliestExecutions.contains(currentNode.getExecution())) {
            earliestExecutions.remove(currentNode.getExecution());

            for (ModuleExecutionNode childNode : currentNode.getInputExecutions()) {
                if (earliestExecutions.contains(childNode.getExecution())) {
                    return false;
                }
            }

            earliestExecutions.add(currentNode.getExecution());
            return true;
        }

        return foundTargetId;
    }
}
