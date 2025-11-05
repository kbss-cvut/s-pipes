package cz.cvut.spipes.riot;

import org.apache.jena.graph.Node;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Performs a topological ordering of the module graph defined by {@code sm:next} edges,
 * returning nodes in their intended execution order.
 * <p>
 * This algorithm is a modified version of Kahn's topological sort with a custom priority:
 * <ul>
 *   <li>First, the in-degree (number of incoming edges) is computed for all nodes.</li>
 *   <li>All source nodes (with in-degree = 0) are inserted into a priority queue with step = 0.</li>
 *   <li>The priority queue orders entries by:
 *     <ol>
 *       <li>Step (descending): nodes discovered later have higher priority, ensuring
 *           that traversal prefers to continue along the current chain of edges.</li>
 *       <li>Lexicographic order of the node's string representation (ascending),
 *           used to break ties within the same step.</li>
 *     </ol>
 *   </li>
 *   <li>At each iteration, the node with the highest step (and lexicographically smallest
 *       string within that step) is dequeued and added to the result.</li>
 *   <li>For each of its successors, the in-degree is decremented. If a successor's in-degree
 *       reaches zero, it is enqueued with {@code step = parent.step + 1}.</li>
 *   <li>This ensures that the algorithm follows edges as deeply as possible before
 *       switching to alternative branches, while still producing a valid topological order.</li>
 * </ul>
 *
 * <pre>
 * Example:
 *   A → B
 *   A → C
 *   B → D
 *
 * Resulting order: [A, B, D, C]
 * </pre>
 */
public class SPipesExecutionOrder {
    private Map<Node, List<Node>> edges;

    public SPipesExecutionOrder(Map<Node, List<Node>> edges) {
        this.edges = edges;
    }

    protected List<Node> compute() {
        Map<Node, Integer> indegree = computeIndegree();
        Queue<QueueEntry> q = indegree.entrySet().stream()
                .filter(e -> e.getValue() == 0)
                .map(e -> new QueueEntry(e.getKey(), 0))
                .collect(Collectors.toCollection(() -> new PriorityQueue<>(queueComparator())));

        List<Node> order = new ArrayList<>();
        while (!q.isEmpty()) {
            QueueEntry entry = q.poll();
            order.add(entry.node());
            processSuccessors(entry.node, entry.step, indegree, q);
        }

        if (order.size() < indegree.size()) {
            throw new IllegalStateException("Cycle detected in sm:next graph");
        }
        return order;
    }

    private Comparator<QueueEntry> queueComparator() {
        return Comparator
                .comparingInt(QueueEntry::step).reversed()
                .thenComparing(e -> e.node().toString());
    }

    private void processSuccessors(Node u, int step,
                                   Map<Node, Integer> indegree,
                                   Queue<QueueEntry> q) {
        for (Node v : edges.getOrDefault(u, List.of())) {
            int deg = indegree.merge(v, -1, Integer::sum);
            if (deg == 0) {
                q.add(new QueueEntry(v, step + 1));
            }
        }
    }

    private Map<Node, Integer> computeIndegree() {
        Map<Node, Integer> indegree = new HashMap<>();
        edges.forEach((u, vs) -> {
            indegree.putIfAbsent(u, 0);
            vs.forEach(v -> indegree.merge(v, 1, Integer::sum));
        });
        return indegree;
    }

    private record QueueEntry(Node node, int step) {}
}