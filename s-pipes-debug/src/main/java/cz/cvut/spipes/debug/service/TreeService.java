package cz.cvut.spipes.debug.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Models;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.cvut.spipes.debug.mapper.ModuleExecutionMapper;
import cz.cvut.spipes.debug.persistance.dao.ModuleExecutionDao;
import cz.cvut.spipes.debug.tree.ExecutionTree;
import cz.cvut.spipes.debug.tree.ModuleExecutionNode;
import cz.cvut.spipes.model.ModuleExecution;

@Service
public class TreeService {

    private final ModuleExecutionDao moduleExecutionDao;

    private final ModuleExecutionMapper moduleExecutionMapper;

    @Autowired
    public TreeService(ModuleExecutionDao moduleExecutionDao, ModuleExecutionMapper moduleExecutionMapper) {
        this.moduleExecutionDao = moduleExecutionDao;
        this.moduleExecutionMapper = moduleExecutionMapper;
    }

    public ModuleExecution findFirstOutputDifference(ExecutionTree tree1, ExecutionTree tree2) {
        ModuleExecutionNode moduleExecutionNode1 = tree1.getRootNode();
        ModuleExecutionNode moduleExecutionNode2 = tree2.getRootNode();
        List<ModuleExecutionNode> leaves1 = collectAllNodesByDepth(moduleExecutionNode1);
        List<ModuleExecutionNode> leaves2 = collectAllNodesByDepth(moduleExecutionNode2);

        for (int i = leaves1.size() - 1; i >= 0; i--) {
            ModuleExecutionNode leaf1 = leaves1.get(i);
            ModuleExecutionNode leaf2 = leaves2.get(i);

            ModuleExecution result = compareNodes(leaf1, leaf2);
            if (result != null) {
                return  result;
            }
        }
        return null;
    }

    private List<ModuleExecutionNode> collectAllNodesByDepth(ModuleExecutionNode node) {
        List<ModuleExecutionNode> leaves = new ArrayList<>();
        Queue<ModuleExecutionNode> queue = new LinkedList<>();
        queue.add(node);

        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                ModuleExecutionNode currentNode = queue.poll();
                leaves.add(currentNode);
                queue.addAll(currentNode.getInputExecutions());
            }
        }
        return leaves;
    }

    private ModuleExecution compareNodes(ModuleExecutionNode node1, ModuleExecutionNode node2) {
        if (!node1.getExecution().getHas_module_id().equals(node2.getExecution().getHas_module_id())) {
            throw new IllegalArgumentException("Structure of scripts is different!");
        }
        String outputIri1 = node1.getExecution().getHas_rdf4j_output().getId();
        String outputIri2 = node2.getExecution().getHas_rdf4j_output().getId();
        Set<Statement> statements1 = moduleExecutionDao.getModelForOutputContext(outputIri1);
        Set<Statement> statements2 = moduleExecutionDao.getModelForOutputContext(outputIri2);
        if (!areModulesIsomorphic(statements1, statements2)) {
            return node1.getExecution();
        }
        return null;
    }

    private boolean areModulesIsomorphic(Set<Statement> statements1, Set<Statement> statements2) {
        return Models.isomorphic(statements1, statements2);
    }
}
