package cz.cvut.spipes.util;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.atlas.io.AWriter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SPipesFormatter {

    private final Model model;
    private final Map<String, String> ns;
    private final Map<Node, Map<Node, List<Node>>> subjectMap = new LinkedHashMap<>();
    private final Map<String, Integer> inDegree = new HashMap<>();
    private final Map<String, String> bnodeLabels = new LinkedHashMap<>();
    private int bCounter = 0;

    private final SPipesNodeFormatter nodeFormatter;

    public SPipesFormatter(Model model) {
        this.model = model;
        this.ns = model.getNsPrefixMap();
        this.nodeFormatter = new SPipesNodeFormatter(model, ns, inDegree, bnodeLabels, bCounter);
        buildSubjectMap();
        for (Node subj : subjectMap.keySet())
            if (subj.isBlank() && inDegreeOf(subj) > 1) bnodeLabels.put(subj.getBlankNodeLabel(), "_:b" + (bCounter++));
    }

    private void buildSubjectMap() {
        Iterator<Triple> it = model.getGraph().find();
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
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), false);
        AWriter aw = new SimpleAWriter(pw);
        writePrefixes(aw);
        writeTriples(aw);
        aw.flush();
    }

    private void writePrefixes(AWriter w) {
        List<String> priority = List.of("owl", "rdf", "rdfs", "skos", "sm", "sml", "sp", "spin", "xsd");
        Comparator<Map.Entry<String, String>> prefixComparator = (e1, e2) -> {
            if (e1.getKey().isEmpty() && !e2.getKey().isEmpty()) return -1;
            if (!e1.getKey().isEmpty() && e2.getKey().isEmpty()) return 1;
            int i1 = priority.indexOf(e1.getKey());
            int i2 = priority.indexOf(e2.getKey());
            if (i1 != -1 && i2 != -1) return Integer.compare(i1, i2);
            if (i1 != -1) return -1;
            if (i2 != -1) return 1;
            return e1.getKey().compareToIgnoreCase(e2.getKey());
        };

        ns.entrySet().stream()
                .sorted(prefixComparator)
                .forEach(e -> w.print(String.format("@prefix %s: <%s> .%n", e.getKey(), e.getValue())));

        w.println();
    }

    private final Comparator<Node> PRED_ORDER = (p1,p2)->{
        if(p1.equals(p2)) return 0;
        if(RDF.type.asNode().equals(p1)) return -1;
        if(RDF.type.asNode().equals(p2)) return 1;
        return (p1.isURI()?p1.getURI():p1.toString()).compareTo(p2.isURI()?p2.getURI():p2.toString());
    };

    private void writeTriples(AWriter w) {
        List<Node> subjects = new ArrayList<>(subjectMap.keySet());
        subjects.sort((a,b)->{
            int ca=a.isURI()?0:hasLabel(a)?1:2, cb=b.isURI()?0:hasLabel(b)?1:2;
            if(ca!=cb) return Integer.compare(ca, cb);
            if(a.isURI() && b.isURI()) return a.getURI().compareTo(b.getURI());
            if(hasLabel(a)&&hasLabel(b)) return bnodeLabels.get(a.getBlankNodeLabel())
                    .compareTo(bnodeLabels.get(b.getBlankNodeLabel()));
            return 0;
        });
        for(Node subject : subjects){
            if(subject.isBlank() && !hasLabel(subject) && inDegreeOf(subject)>=1) continue;
            nodeFormatter.format(w, subject); w.println();
            Map<Node,List<Node>> predMap = new TreeMap<>(PRED_ORDER);
            predMap.putAll(subjectMap.getOrDefault(subject, Collections.emptyMap()));
            if(predMap.isEmpty()){w.println("    .\n"); continue;}
            for(Map.Entry<Node,List<Node>> e:predMap.entrySet()){
                Node pred=e.getKey(); boolean isA = RDF.type.asNode().equals(pred);
                w.print("    "); if(isA) w.print("a "); else nodeFormatter.format(w,pred); w.print(isA?"":" ");
                for(Node obj:e.getValue()){ nodeFormatter.formatNodeWithPath(w,obj,new HashSet<>()); w.println(" ;"); w.print("    "); }
            }
            w.println("    .\n");
        }
    }
}
