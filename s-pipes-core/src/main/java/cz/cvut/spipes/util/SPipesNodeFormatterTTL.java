package cz.cvut.spipes.util;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.riot.out.NodeFormatterTTL_MultiLine;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapStd;
import org.apache.jena.vocabulary.RDF;

import java.util.*;

public class SPipesNodeFormatterTTL {

    final NodeFormatterTTL_MultiLine delegate;
    private final Graph graph;
    private final Map<String,Integer> inDegree;
    private final Map<String,String> bnodeLabels;

    /**
     * Formats individual RDF nodes for Turtle output.
     * Handles blank nodes with custom logic for inlining and labelling.
     * Delegates to Jena's NodeFormatterTTL_Multiline when appropriate.
     */
    public SPipesNodeFormatterTTL(Graph graph,
                                  Map<String,String> ns,
                                  Map<String, Integer> inDegree,
                                  Map<String, String> bnodeLabels) {
        this.graph = graph;
        this.inDegree = inDegree;
        this.bnodeLabels = bnodeLabels;
        PrefixMap prefixMap = new PrefixMapStd();
        ns.forEach(prefixMap::add);
        this.delegate = new NodeFormatterTTL_MultiLine(null, prefixMap);
    }

    /**
     * Entry point for formatting any RDF node.
     * Delegates to specific formatting methods based on node type.
     * For non-blank nodes, formatting is delegated to NodeFormatterTTL_Multiline.
     *
     * @param w the writer to output to
     * @param node the RDF node to format
     * @param path used to detect cycles when formatting blank nodes
     */
    public void formatNode(AWriter w, Node node, Set<Node> path) {
        if (node.isBlank()) {
            formatBNode(w, node, path);
        } else {
            delegate.format(w, node);
        }
    }

    /**
     * Formats a blank node either inline or with a stable label.
     * If the node has a label in {@code bnodeLabels}, prints it directly.
     * Otherwise, checks its in-degree:
     * - If ≤ 1, expands inline as a Turtle property list.
     * - If > 1, assigns a label like {@code _:bX} to preserve identity.
     *
     * @param w the writer to output to
     * @param node the blank node to format
     * @param path used to detect cycles
     */
    private void formatBNode(AWriter w, Node node, Set<Node> path) {
        String label = node.getBlankNodeLabel();

        if (bnodeLabels.containsKey(label)) {
            w.print(bnodeLabels.get(label));
            return;
        }

        if (inDegree.getOrDefault(label, 0) <= 1) {
            formatBNodeAsPropertyList(w, node, path);
        } else {
            w.print("_:" + label);
        }
    }

    /**
     * Expands a blank node inline using Turtle's {@code [ ... ]} syntax.
     * Formats all predicate–object pairs inside the node.
     *
     * @param w the writer to output to
     * @param blank the blank node to expand
     * @param path used to detect cycles
     */
    private void formatBNodeAsPropertyList(AWriter w, Node blank, Set<Node> path) {
        if (!path.add(blank)) {
            w.print("_:" + blank.getBlankNodeLabel());
            return;
        }

        List<Triple> props = graph.find(blank, Node.ANY, Node.ANY).toList();

        if (props.isEmpty()) {
            w.print("[]");
            path.remove(blank);
            return;
        }

        w.print("[ ");
        props.stream()
                .sorted(Comparator.comparing(Triple::getPredicate, PRED_ORDER))
                .forEach(t -> printProperty(w, t, path));
        w.print("]");
        path.remove(blank);
    }

    /**
     * Prints a single predicate–object pair inside a property list.
     * If the predicate is {@code rdf:type}, prints {@code a}.
     * Otherwise, delegates formatting to NodeFormatterTTL_Multiline.
     *
     * @param w the writer to output to
     * @param t the predicate–object pair to print
     * @param path used to detect cycles
     */
    private void printProperty(AWriter w, Triple t, Set<Node> path) {
        Node p = t.getPredicate();
        Node o = t.getObject();

        formatPredicate(w, p);

        w.print(" ");
        formatNode(w, o, path);
        w.print(" ; ");
    }

    /**
     * Formats a predicate node.
     * If the predicate is {@code rdf:type}, prints {@code a}.
     * Otherwise, delegates formatting to NodeFormatterTTL_Multiline.
     *
     * @param w the writer to output to
     * @param predicate the predicate node to format
     */
    protected void formatPredicate(AWriter w, Node predicate) {
        if (predicate.equals(RDF.type.asNode())) {
            w.print("a");
        } else {
            delegate.format(w, predicate);
        }
    }

    // Comparator where rdf:type ("a") always comes first, then lexicographical order
    protected static final Comparator<Node> PRED_ORDER =
            Comparator.<Node>comparingInt(p -> RDF.type.asNode().equals(p) ? 0 : 1)
                    .thenComparing((Node n) -> n.toString());
}
