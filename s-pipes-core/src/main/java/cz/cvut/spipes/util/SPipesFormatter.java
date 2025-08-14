package cz.cvut.spipes.util;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class SPipesFormatter {

    private static final Comparator<Property> PREDICATE_ORDER = (p1, p2) -> {
        if (p1.equals(p2)) return 0;
        if (RDF.type.equals(p1)) return -1;
        if (RDF.type.equals(p2)) return 1;
        return p1.getURI().compareTo(p2.getURI());
    };

    private final Model model;
    private final Map<String, String> ns;
    private final Map<Resource, Map<Property, List<RDFNode>>> subjectMap = new LinkedHashMap<>();

    private final Map<Resource, Integer> inDegree = new HashMap<>();
    private final Map<Resource, String> bnodeLabels = new LinkedHashMap<>();
    private int bCounter = 0;

    public SPipesFormatter(Model model) {
        this.model = model;
        this.ns = model.getNsPrefixMap();
        buildSubjectMap();
    }

    public void writeTo(OutputStream outputStream) {
        var writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), false);
        writePrefixes(writer);
        writeTriples(writer);
        writer.flush();
    }

    private void buildSubjectMap() {
        StmtIterator stmtIter = model.listStatements();
        while (stmtIter.hasNext()) {
            Statement stmt = stmtIter.nextStatement();
            Resource subj = stmt.getSubject();
            Property pred = stmt.getPredicate();
            RDFNode obj = stmt.getObject();

            subjectMap
                    .computeIfAbsent(subj, k -> new LinkedHashMap<>())
                    .computeIfAbsent(pred, k -> new ArrayList<>())
                    .add(obj);

            if (obj.isAnon()) {
                Resource br = obj.asResource();
                inDegree.merge(br, 1, Integer::sum);
            }
        }
        for (Resource subj : subjectMap.keySet()) {
            if (subj.isAnon() && inDegreeOf(subj) > 1) {
                allocLabel(subj);
            }
        }
    }

    private void writePrefixes(PrintWriter writer) {
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
                .forEach(e -> writer.printf("@prefix %s: <%s> .%n", e.getKey(), e.getValue()));

        writer.println();
    }


    private void writeTriples(PrintWriter writer) {
        List<Resource> subjects = getSubjects();

        for (Resource subject : subjects) {
            if (subject.isAnon() && !hasLabel(subject) && inDegreeOf(subject) >= 1) {
                continue;
            }
            if (subject.isAnon() && !hasLabel(subject) && inDegreeOf(subject) == 0) {
                writer.println(formatBNodeAsPropertyList(subject, new HashSet<>()));
                continue;
            }

            writer.println(formatNode(subject));

            Map<Property, List<RDFNode>> predMap = new TreeMap<>(PREDICATE_ORDER);
            predMap.putAll(subjectMap.get(subject));

            if (predMap.isEmpty()) {
                writer.println("    .\n");
                continue;
            }

            List<Map.Entry<Property, List<RDFNode>>> predEntries = new ArrayList<>(predMap.entrySet());
            for (Map.Entry<Property, List<RDFNode>> predEntry : predEntries) {
                String predStr = RDF.type.equals(predEntry.getKey()) ? "a" : formatNode(predEntry.getKey());

                List<String> objStrs = predEntry.getValue().stream()
                        .map(this::formatNode)
                        .toList();

                for (String objStr : objStrs) {
                    writer.println("    " + predStr + " " + objStr + " ;");
                }
            }

            writer.println("    .");
        }
    }

    @NotNull
    private List<Resource> getSubjects() {
        List<Resource> subjects = new ArrayList<>(subjectMap.keySet());
        subjects.sort((a, b) -> {
            int ca = a.isURIResource() ? 0 : (hasLabel(a) ? 1 : 2);
            int cb = b.isURIResource() ? 0 : (hasLabel(b) ? 1 : 2);
            if (ca != cb) return Integer.compare(ca, cb);
            if (a.isURIResource() && b.isURIResource()) return a.getURI().compareTo(b.getURI());
            if (hasLabel(a) && hasLabel(b)) return getLabel(a).compareTo(getLabel(b));
            return 0;
        });
        return subjects;
    }

    private String formatNode(RDFNode node) {
        if (node.isLiteral()) {
            return formatLiteral(node.asLiteral());
        } else if (node.isAnon()) {
            Resource br = node.asResource();
            if (hasLabel(br)) return getLabel(br);
            return formatBNodeAsPropertyList(br, new HashSet<>());
        } else if (node.isURIResource()) {
            return formatURI(node.asResource());
        } else {
            return node.toString();
        }
    }

    private String formatURI(Resource res) {
        String uri = res.getURI();
        for (var e : ns.entrySet()) {
            if (uri.startsWith(e.getValue())) {
                return e.getKey() + ":" + uri.substring(e.getValue().length());
            }
        }
        return "<" + uri + ">";
    }

    private String formatLiteral(Literal lit) {
        String value = lit.getString();
        boolean multiline = value.contains("\n") || value.contains("\r");
        String escaped = escapeString(value, multiline);
        String lex = multiline ? "\"\"\"" + escaped + "\"\"\"" : "\"" + escaped + "\"";

        String lang = lit.getLanguage();
        if (lang != null && !lang.isEmpty()) {
            return lex + "@" + lang;
        }

        String dt = lit.getDatatypeURI();
        if (dt != null && !dt.equals(XSDDatatype.XSDstring.getURI())) {
            return lex + "^^" + formatURI(ResourceFactory.createResource(dt));
        }

        return lex;
    }

    private String escapeString(String s, boolean multiline) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': b.append("\\\\"); break;
                case '"':
                    if (!multiline) {
                        b.append("\\\"");
                    } else {
                        if (i + 2 < s.length() && s.charAt(i+1) == '"' && s.charAt(i+2) == '"') {
                            b.append("\\\"\\\"\\\"");
                            i += 2;
                        } else {
                            b.append('"');
                        }
                    }
                    break;
                case '\n': b.append(multiline ? "\n" : "\\n"); break;
                case '\r': b.append(multiline ? "\r" : "\\r"); break;
                case '\t': b.append("\\t"); break;
                case '\b': b.append("\\b"); break;
                case '\f': b.append("\\f"); break;
                default:
                    if (c < 0x20) b.append(String.format("\\u%04X", (int) c));
                    else b.append(c);
            }
        }
        return b.toString();
    }

    private String formatBNodeAsPropertyList(Resource blank, Set<Resource> path) {
        if (hasLabel(blank)) return getLabel(blank);
        if (!path.add(blank)) return allocLabel(blank);

        List<Statement> props = model.listStatements(blank, null, (RDFNode) null).toList();
        if (props.isEmpty()) return "[]";

        props.sort(Comparator
                .comparing(Statement::getPredicate, PREDICATE_ORDER)
                .thenComparing(s -> formatNode(s.getObject())));

        StringBuilder builder = new StringBuilder("[ ");
        for (Statement stmt : props) {
            String predStr = stmt.getPredicate().equals(RDF.type) ? "a" : formatNode(stmt.getPredicate());
            String objStr = formatNodeWithPath(stmt.getObject(), path);
            builder.append(predStr).append(" ").append(objStr).append(" ; ");
        }
        builder.append("]");
        path.remove(blank);
        return builder.toString();
    }

    private String formatNodeWithPath(RDFNode node, Set<Resource> path) {
        if (node.isAnon()) {
            Resource br = node.asResource();
            if (hasLabel(br)) return getLabel(br);
            if (inDegreeOf(br) <= 1) return formatBNodeAsPropertyList(br, path);
            return allocLabel(br);
        }
        return formatNode(node);
    }

    private int inDegreeOf(Resource r) { return inDegree.getOrDefault(r, 0); }
    private boolean hasLabel(Resource r) { return bnodeLabels.containsKey(r); }
    private String getLabel(Resource r) { return bnodeLabels.get(r); }
    private String allocLabel(Resource r) {
        return bnodeLabels.computeIfAbsent(r, k -> "_:b" + (bCounter++));
    }

}
