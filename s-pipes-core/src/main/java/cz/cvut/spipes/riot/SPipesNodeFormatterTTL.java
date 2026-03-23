package cz.cvut.spipes.riot;

import cz.cvut.spipes.spin.vocabulary.SP;
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
    private static final int INDENT_STEP = 4;
    final NodeFormatterTTL_MultiLine delegate;
    private final Graph graph;
    private final Map<String,Integer> inDegree;
    private final Map<String,String> bnodeLabels;
    private final Comparator<Node> objectComparator;
    private int indentLevel = 0;

    /**
     * Creates a formatter for RDF nodes in Turtle syntax with custom handling
     * of blank nodes and namespace prefixes.
     *
     * @param graph        the RDF graph whose nodes will be formatted; used to
     *                     determine context when rendering blank nodes
     * @param ns           mapping of namespace prefixes to full URIs; added to
     *                     the internal {@link PrefixMap} for compact Turtle output
     * @param inDegree     map of node identifiers to their in-degree counts;
     *                     used to decide whether a blank node can be inlined
     *                     or should be given a label
     * @param bnodeLabels  mapping of blank node identifiers to stable labels;
     *                     ensures consistent output across multiple serializations
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

        this.objectComparator = Comparator.<Node>comparingInt(n -> {
            if (n.isURI()) return 0;
            if (n.isLiteral()) return 1;
            if (hasLabel(n, this.bnodeLabels)) return 2;
            if (n.isBlank()) return 3;
            return 4;
        }).thenComparing(n -> {
            if (n.isURI()) return n.getURI();
            if (n.isLiteral()) return n.getLiteralLexicalForm();
            if (hasLabel(n, this.bnodeLabels)) return this.bnodeLabels.get(n.getBlankNodeLabel());
            if (n.isBlank()) {
                String sparqlKey = getSparqlQuerySortKey(n);
                if (sparqlKey != null) return sparqlKey;
                return n.getBlankNodeLabel();
            }
            return "";
        });
    }

    boolean hasLabel(Node n, Map<String, String> bnodeLabels) { return n.isBlank() && bnodeLabels.containsKey(n.getBlankNodeLabel()); }

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

        List<Triple> triples = graph.find(blank, Node.ANY, Node.ANY).toList();
        if (triples.isEmpty()) {
            w.print("[]");
        } else {
            w.print("[\n");
            indentLevel++;
            List<Triple> sorted = triples.stream()
                    .sorted(Comparator
                            .comparing(Triple::getPredicate, PRED_ORDER)
                            .thenComparing(t -> t.getObject().toString()))
                    .toList();
            for (Triple t : sorted) {
                printIndent(w);
                printProperty(w, t, path);
                w.print("\n");
            }
            indentLevel--;
            printIndent(w);
            w.print("]");
        }
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
        w.print(" ;");
    }

    void setIndentLevel(int level) {
        this.indentLevel = level;
    }

    private void printIndent(AWriter w) {
        w.print(" ".repeat(indentLevel * INDENT_STEP));
    }
    /**
     * Formats a predicate node.
     * If the predicate is {@code rdf:type}, prints {@code a}.
     * Otherwise, delegates formatting to NodeFormatterTTL_Multiline.
     *
     * @param w the writer to output to
     * @param predicate the predicate node to format
     */
    void formatPredicate(AWriter w, Node predicate) {
        if (predicate.equals(RDF.type.asNode())) {
            w.print("a");
        } else {
            delegate.format(w, predicate);
        }
    }

    /**
     * Returns a deterministic sort key for blank nodes representing SPARQL queries.
     * For blank nodes typed as {@code sp:Construct}, {@code sp:Ask}, or {@code sp:Select},
     * extracts the {@code sp:text} value. If the text starts with {@code #}, the full text
     * is used as the key; otherwise an empty string is returned so that uncommented queries
     * sort before commented ones. Returns {@code null} for non-SPARQL-query blank nodes.
     */
    private String getSparqlQuerySortKey(Node n) {
        if (!n.isBlank()) return null;

        Node typeNode = RDF.type.asNode();
        boolean isSparqlQuery =
            graph.contains(n, typeNode, SP.Ask.asNode()) ||
            graph.contains(n, typeNode, SP.Construct.asNode()) ||
            graph.contains(n, typeNode, SP.Select.asNode());

        if (!isSparqlQuery) return null;

        Node spTextNode = SP.text.asNode();
        Iterator<Triple> it = graph.find(n, spTextNode, Node.ANY);
        if (!it.hasNext()) return null;

        String text = it.next().getObject().getLiteralLexicalForm();
        return text.startsWith("#") ? text : "";
    }

    // Comparator where rdf:type ("a") always comes first, then lexicographical order
    final Comparator<Node> PRED_ORDER =
            Comparator.<Node>comparingInt(p -> RDF.type.asNode().equals(p) ? 0 : 1)
                    .thenComparing((Node n) -> n.toString());

    /**
          Comparator for RDF {@link Node} objects used when ordering object positions
          in Turtle serialisation.

          <p>The comparison is performed in two stages:</p>
          <ol>
            <li>By node type, in the following priority:
                <ul>
                  <li>URIs (rank 0)</li>
                  <li>Literals (rank 1)</li>
                  <li>Blank nodes with assigned labels (rank 2)</li>
                  <li>Unlabeled blank nodes (rank 3)</li>
                  <li>Other node types (rank 4)</li>
                </ul>
            </li>
            <li>Within the same category, nodes are ordered lexicographically by:
                <ul>
                  <li>URI string for URIs</li>
                  <li>Lexical form for literals</li>
                  <li>Assigned label for labeled blank nodes</li>
                  <li>For unlabeled blank nodes typed as {@code sp:Construct}, {@code sp:Ask},
                      or {@code sp:Select}: by their {@code sp:text} value. Texts starting
                      with {@code #} are sorted by the full text; texts without a leading
                      {@code #} sort first (empty key). This ensures stable ordering of
                      SPARQL query blank nodes across parser runs.</li>
                  <li>Other unlabeled blank nodes by internal blank node identifier</li>
                </ul>
            </li>
          </ol>

          <p>This ensures stable and human-readable ordering of objects in Turtle output,
          especially when blank nodes are involved.</p>
         */
    public Comparator<Node> getObjectComparator() {
        return objectComparator;
    }
}