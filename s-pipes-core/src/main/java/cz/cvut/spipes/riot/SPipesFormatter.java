package cz.cvut.spipes.riot;

import cz.cvut.spipes.constants.SM;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import java.io.OutputStream;
import java.util.*;
import static org.apache.jena.riot.system.RiotLib.writePrefixes;
/**
 * Formats an RDF graph into Turtle syntax with custom structure and blank node handling.
 * Controls subject ordering, predicate sorting, and punctuation.
 * Uses {@link SPipesNodeFormatterTTL} for node-level formatting.
 *
 * <h3>Formatting Rules:</h3>
 * <ul>
 *   <li>Subject blocks end with {@code .} on a separate line (see {@link #writeTriples})</li>
 *   <li>Subject order: ontology, URIs, blank nodes (see {@link #sortSubjects})</li>
 *   <li>Type declaration using {@code a} comes first in each subject block
 *       (see {@link SPipesNodeFormatterTTL#PRED_ORDER})</li>
 *   <li>Multiline literals are formatted by default using {@code """}.
 *       If the string contains {@code "} but not {@code '}, then {@code '''} is used instead
 *       (see {@link org.apache.jena.riot.out.NodeFormatterTTL_MultiLine})</li>
 * </ul>
 *
 * <h3>Example TTL Output:</h3>
 * <pre>
 * :construct-greeting
 *     a sml:ApplyConstruct ;
 *     sm:next :express-greeting_Return ;
 *     sml:constructQuery [ a sp:Construct ; sp:text '''
 *           CONSTRUCT {
 *             &lt;http://example.com/person1&gt; :is-greeted-by-message ?greetingMessage .
 *           } WHERE {
 *             BIND(concat("Hello world") as ?greetingMessage)
 *           }
 *       ''' ; ] ;
 *     sml:replace true ;
 * .
 * </pre>
 */
public class SPipesFormatter {
    private final Graph graph;
    private final Map<String, String> ns;
    private final Map<Node, Map<Node, List<Node>>> subjectMap = new LinkedHashMap<>();
    private final Map<String, Integer> inDegree = new HashMap<>();
    private final Map<String, String> bnodeLabels = new LinkedHashMap<>();
    private int bCounter = 0;
    private final Node smNext = NodeFactory.createURI(SM.next);
    private final Set<Node> modules = new HashSet<>();
    private final Map<Node, List<Node>> edges = new HashMap<>();
    private final Map<Node, Integer> topoIndex = new HashMap<>();

    private final SPipesNodeFormatterTTL nodeFormatter;

    public SPipesFormatter(Graph graph, PrefixMap prefixMap) {
        this.graph = graph;
        this.ns = new LinkedHashMap<>(prefixMap.getMapping());
        buildSubjectMap();
        assignBNodeLabels();
        this.nodeFormatter = new SPipesNodeFormatterTTL(graph, ns, inDegree, bnodeLabels);
        List<Node> topoOrder = new SPipesExecutionOrder(edges).compute();
        for (int i = 0; i < topoOrder.size(); i++) {
            topoIndex.put(topoOrder.get(i), i);
        }
    }

    /**
     * Assigns stable labels to blank nodes that are referenced more than once.
     * Labels are stored in {@code bnodeLabels} as {@code _:b0}, {@code _:b1}, etc.
     */
    private void assignBNodeLabels() {
        for (Node subj : subjectMap.keySet()) {
            if (subj.isBlank() && inDegreeOf(subj) > 1) {
                bnodeLabels.put(subj.getBlankNodeLabel(), "_:b" + (bCounter++));
            }
        }
    }
    /**
     * Builds a nested map of triples: {@code subject → predicate → list of objects}.
     * Also tracks in-degree of blank nodes (how often they appear as objects).
     */
    private void buildSubjectMap() {
        graph.stream().forEach(
                t -> {
                    Node s = t.getSubject(), p = t.getPredicate(), o = t.getObject();
                    subjectMap.computeIfAbsent(s, k -> new LinkedHashMap<>())
                            .computeIfAbsent(p, k -> new ArrayList<>()).add(o);
                    if (t.getPredicate().equals(smNext)) {
                        modules.add(t.getSubject());
                        modules.add(t.getObject());
                        edges.computeIfAbsent(s, k -> new ArrayList<>()).add(o);
                    }
                    if (o.isBlank()) inDegree.merge(o.getBlankNodeLabel(), 1, Integer::sum);
                }
        );
    }

    private int inDegreeOf(Node n) { return n.isBlank() ? inDegree.getOrDefault(n.getBlankNodeLabel(), 0) : 0; }

    /**
     * Entry point for serialising the graph to Turtle.
     * Writes prefix declarations and serialises all triples.
     *
     * @param out the output stream to write to
     */
    public void writeTo(OutputStream out) {
        try(IndentedWriter aw = new IndentedWriter(out)){
            PrefixMap prefixMap = PrefixMapFactory.createForOutput();
            ns.forEach(prefixMap::add);
            writePrefixes(aw, prefixMap);
            aw.println();
            writeTriples(aw);
            aw.flush();
        }
    }
    /**
     * Serializes all triples in the graph.
     * Subjects are sorted using {@code SUBJECT_COMPARATOR}.
     * Skips blank nodes that are referenced elsewhere and not labelled.
     * Each subject block ends with {@code .} and a newline.
     *
     * @param w the writer to output to
     */
    private void writeTriples(AWriter w) {
        List<Node> subjects = sortSubjects(new ArrayList<>(subjectMap.keySet()));

        for (Node subject : subjects) {
            if (subject.isBlank() && !nodeFormatter.hasLabel(subject, bnodeLabels) && inDegreeOf(subject) >= 1) continue;

            nodeFormatter.formatNode(w, subject, new HashSet<>());
            w.println();

            Map<Node, List<Node>> predMap = new TreeMap<>(nodeFormatter.PRED_ORDER);
            predMap.putAll(subjectMap.getOrDefault(subject, Collections.emptyMap()));

            for (Map.Entry<Node, List<Node>> entry : predMap.entrySet()) {
                entry.getValue().sort(nodeFormatter.OBJECT_COMPARATOR);
            }

            if (!predMap.isEmpty()) {
                writePredicates(w, predMap);
            }
            w.println(".\n");
        }
    }
    /**
     * Defines categories for sorting subjects:
     * - {@code ONTOLOGY}: subjects typed as {@code owl:Ontology}
     * - {@code URI}: subjects with URIs
     * - {@code LABELED_BNODE}: blank nodes with assigned labels
     * - {@code OTHER}: all other blank nodes
     */
    private enum NodeCategory {
        ONTOLOGY, URI, LABELED_BNODE, OTHER
    }
    /**
     * Determines the {@link NodeCategory} of a node for sorting purposes.
     *
     * @param n the node to categorise
     * @return the category of the node
     */
    private NodeCategory category(Node n) {
        Map<Node, List<Node>> preds = subjectMap.get(n);
        if (preds != null && preds.getOrDefault(RDF.type.asNode(), List.of())
                .contains(OWL.Ontology.asNode())) {
            return NodeCategory.ONTOLOGY;
        }
        if (n.isURI()) return NodeCategory.URI;
        if (nodeFormatter.hasLabel(n, bnodeLabels)) return NodeCategory.LABELED_BNODE;
        return NodeCategory.OTHER;
    }

    /**
     * Sorts subjects using {@code SUBJECT_COMPARATOR}, which prioritizes:
     * - Priority (ontology &lt; URI &lt; labelled bnode &lt; other)
     * - Topological index (for modules)
     * - Category
     * - URI lexicographically
     * - Label lexicographically
     * @param subjects the list of subjects to sort
     * @return the sorted list
     */
    private List<Node> sortSubjects(List<Node> subjects) {
        subjects.sort(SUBJECT_COMPARATOR());
        return subjects;
    }

    private record SubjectRank(int priority, int topo, NodeCategory category, String uri, String label) {}

    private SubjectRank rank(Node n) {
        return new SubjectRank(
                category(n) == NodeCategory.ONTOLOGY ? 0 : topoIndex.containsKey(n) ? 1 : 2,
                topoIndex.getOrDefault(n, Integer.MAX_VALUE),
                category(n),
                uriOrEmpty(n),
                labelOrEmpty(n)
        );
    }


    private Comparator<Node> SUBJECT_COMPARATOR() {
        return Comparator.comparing(this::rank,
                Comparator.comparingInt(SubjectRank::priority)
                        .thenComparingInt(SubjectRank::topo)
                        .thenComparing(SubjectRank::category)
                        .thenComparing(SubjectRank::uri)
                        .thenComparing(SubjectRank::label)
        );
    }

    private String uriOrEmpty(Node n) {
        return n.isURI() ? n.getURI() : "";
    }

    private String labelOrEmpty(Node n) {
        return nodeFormatter.hasLabel(n, bnodeLabels) ? bnodeLabels.get(n.getBlankNodeLabel()) : "";
    }


    /**
     * Serializes all predicate–object pairs for a given subject.
     * Predicates are sorted using {@code PRED_ORDER}.
     * Uses {@link SPipesNodeFormatterTTL#formatPredicate} to format predicates.
     * Objects are separated by commas ({@code ,}), predicates by semicolons ({@code ;}).
     *
     * @param w the writer to output to
     * @param predMap the predicate–object map for the subject
     */
    private void writePredicates(AWriter w, Map<Node, List<Node>> predMap) {
        for (Map.Entry<Node, List<Node>> e : predMap.entrySet()) {
            Node pred = e.getKey();
            w.print("    ");
            nodeFormatter.formatPredicate(w, pred);
            w.print(" ");
            Iterator<Node> it = e.getValue().iterator();
            while (it.hasNext()) {
                nodeFormatter.formatNode(w, it.next(), new HashSet<>());
                if (it.hasNext()) {
                    w.print(" ,\n    ");
                } else {
                    w.print(" ;");
                }
            }
            w.println();
        }
    }

}