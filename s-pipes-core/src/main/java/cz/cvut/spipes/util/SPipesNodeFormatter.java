package cz.cvut.spipes.util;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapStd;
import org.apache.jena.vocabulary.RDF;

import java.util.*;

public class SPipesNodeFormatter {

    final NodeFormatterTTL delegate;
    private final Model model;
    private final Map<String,String> ns;
    private final Map<String,Integer> inDegree;
    private final Map<String,String> bnodeLabels;

    public SPipesNodeFormatter(Model model,
                               Map<String,String> ns,
                              Map<String, Integer> inDegree,
                              Map<String, String> bnodeLabels) {
        this.model = model;
        this.ns = ns;
        this.inDegree = inDegree;
        this.bnodeLabels = bnodeLabels;
        PrefixMap prefixMap = new PrefixMapStd();
        ns.forEach(prefixMap::add);
        this.delegate = new NodeFormatterTTL(null, prefixMap);
    }

    public void formatNode(AWriter w, Node node, Set<Node> path) {
        if (node.isBlank()) {
            formatBlank(w, node, path);
        } else if (node.isLiteral()) {
            formatLiteral(w, node);
        } else {
            delegate.format(w, node);
        }
    }

    private void formatBlank(AWriter w, Node node, Set<Node> path) {
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

    private void formatLiteral(AWriter w, Node node) {
        String lex = node.getLiteralLexicalForm();
        if (lex.contains("\n")) {
            w.print("\"\"\"" + lex + "\"\"\"");
        } else {
            delegate.format(w, node);
        }
    }


    private void formatBNodeAsPropertyList(AWriter w, Node blank, Set<Node> path) {
        if (!path.add(blank)) {
            w.print("_:" + blank.getBlankNodeLabel());
            return;
        }

        List<Triple> props = model.getGraph().find(blank, Node.ANY, Node.ANY).toList();

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

    private void printProperty(AWriter w, Triple t, Set<Node> path) {
        Node p = t.getPredicate();
        Node o = t.getObject();

        if (p.equals(RDF.type.asNode())) {
            w.print("a");
        } else {
            delegate.format(w, p);
        }

        w.print(" ");
        formatNode(w, o, path);
        w.print(" ; ");
    }

    protected static final Comparator<Node> PRED_ORDER = (p1, p2) -> {
        //rdf:type ("a") always comes first, then lexicographical order
        if (p1.equals(p2)) return 0;
        if (RDF.type.asNode().equals(p1)) return -1;
        if (RDF.type.asNode().equals(p2)) return 1;
        return p1.toString().compareTo(p2.toString());
    };
}
