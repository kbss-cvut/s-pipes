package cz.cvut.spipes.debug.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import cz.cvut.spipes.debug.model.ModuleExecution;

public class ExecutionTree {

    private final ModuleExecutionNode rootNode;

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
                .sorted(Comparator.comparing(ModuleExecution::getHas_module_id))
                .map(ModuleExecutionNode::new)
                .collect(Collectors.toList());

        for (int i = 0; i < executionNodes.size(); i++) {
            if (executionNodes.get(i).getId().equals(rootExecution.getId())) {
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

    public List<ModuleExecution> findEarliest(List<ModuleExecution> findList) {
        List<ModuleExecution> earliestExecutions = new ArrayList<>();
        List<String> targetIds = findList.stream()
                .map(ModuleExecution::getId)
                .collect(Collectors.toList());
        findEarliestRecursive(rootNode, targetIds, earliestExecutions);
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

    public ModuleExecutionNode getRootNode() {
        return rootNode;
    }
}
