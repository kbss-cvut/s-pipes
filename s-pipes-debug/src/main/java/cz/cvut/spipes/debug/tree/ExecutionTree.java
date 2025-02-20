package cz.cvut.spipes.debug.tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import cz.cvut.spipes.model.ModuleExecution;


public class ExecutionTree {

    private final ModuleExecutionNode rootNode;

    public ExecutionTree(Set<ModuleExecution> executionList) {
        rootNode = buildTree(executionList);
    }

    private ModuleExecutionNode buildTree(Set<ModuleExecution> executionList) {
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
                String nextId = null;
                if (executionNode.getExecution().getHas_next() != null) {
                    nextId = executionNode.getExecution().getHas_next().getId();
                }
                String currentId = executionNodes.get(i).getId();
                if (nextId != null && nextId.equals(currentId)) {
                    executionNodes.get(i).addInputExecution(executionNode);
                }
            }
        }
        return rootNode;
    }

    public List<ModuleExecution> findEarliest(List<ModuleExecution> findList) {
        List<ModuleExecutionNode> matchingNodes = new ArrayList<>();
        List<String> targetIds = findList.stream()
                .map(ModuleExecution::getId)
                .collect(Collectors.toList());
        List<ModuleExecutionNode> nodes = findDepthForTargetIds(rootNode, targetIds, matchingNodes, 0);
        Optional<Integer> maxDepth = nodes.stream()
                .map(ModuleExecutionNode::getDepth)
                .max(Integer::compare);

        return nodes.stream()
                .filter(n -> n.getDepth() == maxDepth.get())
                .map(ModuleExecutionNode::getExecution)
                .collect(Collectors.toList());
    }

    private List<ModuleExecutionNode> findDepthForTargetIds(ModuleExecutionNode currentNode, List<String> targetIds, List<ModuleExecutionNode> matchingNodes, int depth) {
        if (targetIds.contains(currentNode.getId())) {
            currentNode.setDepth(depth);
            matchingNodes.add(currentNode);
        }
        for (ModuleExecutionNode moduleExecutionNode : currentNode.getInputExecutions()) {
            findDepthForTargetIds(moduleExecutionNode, targetIds, matchingNodes, depth + 1);
        }
        return matchingNodes;
    }


    public ModuleExecutionNode getRootNode() {
        return rootNode;
    }
}
