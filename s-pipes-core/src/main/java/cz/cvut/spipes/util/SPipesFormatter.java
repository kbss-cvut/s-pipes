package cz.cvut.spipes.util;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.atlas.io.AWriter;

import java.io.OutputStream;
import java.util.*;

import static org.apache.jena.riot.system.RiotLib.writePrefixes;

public class SPipesFormatter {

    private final Graph graph;
    private final Map<String, String> ns;
    private final Map<Node, Map<Node, List<Node>>> subjectMap = new LinkedHashMap<>();
    private final Map<String, Integer> inDegree = new HashMap<>();
    private final Map<String, String> bnodeLabels = new LinkedHashMap<>();
    private int bCounter = 0;

    private final SPipesNodeFormatterTTL nodeFormatter;

    public SPipesFormatter(Graph graph, PrefixMap prefixMap) {
        this.graph = graph;
        this.ns = new LinkedHashMap<>(prefixMap.getMapping());
        this.nodeFormatter = new SPipesNodeFormatterTTL(graph, ns, inDegree, bnodeLabels);
        buildSubjectMap();
        assignBNodeLabels();
    }

    private void assignBNodeLabels() {
        for (Node subj : subjectMap.keySet()) {
            if (subj.isBlank() && inDegreeOf(subj) > 1) {
                bnodeLabels.put(subj.getBlankNodeLabel(), "_:b" + (bCounter++));
            }
        }
    }

    private void buildSubjectMap() {
        Iterator<Triple> it = graph.find();
        while (it.hasNext()) {
            Triple t = it.next();
            Node s = t.getSubject(), p = t.getPredicate(), o = t.getObject();
            subjectMap.computeIfAbsent(s, k -> new LinkedHashMap<>())
                    .computeIfAbsent(p, k -> new ArrayList<>()).add(o);
            if (o.isBlank()) inDegree.merge(o.getBlankNodeLabel(), 1, Integer::sum);
        }
    }

    private int inDegreeOf(Node n) { return n.isBlank() ? inDegree.getOrDefault(n.getBlankNodeLabel(), 0) : 0; }
    private boolean hasLabel(Node n) { return n.isBlank() && bnodeLabels.containsKey(n.getBlankNodeLabel()); }

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

    private void writeTriples(AWriter w) {
        List<Node> subjects = sortSubjects(new ArrayList<>(subjectMap.keySet()));

        for (Node subject : subjects) {
            if (subject.isBlank() && !hasLabel(subject) && inDegreeOf(subject) >= 1) continue;

            nodeFormatter.formatNode(w, subject, null);
            w.println();

            Map<Node, List<Node>> predMap = new TreeMap<>(SPipesNodeFormatterTTL.PRED_ORDER);
            predMap.putAll(subjectMap.getOrDefault(subject, Collections.emptyMap()));

            if (!predMap.isEmpty()) {
                writePredicates(w, predMap);
            }
            w.println(".\n");
        }
    }

    private enum NodeCategory {
        ONTOLOGY, URI, LABELED_BNODE, OTHER
    }

    private NodeCategory category(Node n) {
        Map<Node, List<Node>> preds = subjectMap.get(n);
        if (preds != null && preds.getOrDefault(RDF.type.asNode(), List.of())
                .contains(OWL.Ontology.asNode())) {
            return NodeCategory.ONTOLOGY;
        }
        if (n.isURI()) return NodeCategory.URI;
        if (hasLabel(n)) return NodeCategory.LABELED_BNODE;
        return NodeCategory.OTHER;
    }

    // URIs first, then labeled bnodes, then other bnodes
    private final Comparator<Node> SUBJECT_COMPARATOR =
            Comparator.comparing(this::category)
                    .thenComparing(n -> n.isURI() ? n.getURI() : "")
                    .thenComparing(n -> hasLabel(n) ? bnodeLabels.get(n.getBlankNodeLabel()) : "");

    private List<Node> sortSubjects(List<Node> subjects) {
        subjects.sort(SUBJECT_COMPARATOR);
        return subjects;
    }

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
