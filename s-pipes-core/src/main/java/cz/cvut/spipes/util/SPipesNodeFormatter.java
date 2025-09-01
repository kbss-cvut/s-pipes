package cz.cvut.spipes.util;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.out.NodeFormatterBase;
import org.apache.jena.vocabulary.RDF;

import java.util.*;

public class SPipesNodeFormatter extends NodeFormatterBase {

    private final Model model;
    private final Map<String,String> ns;
    private final Map<String,Integer> inDegree;
    private final Map<String,String> bnodeLabels;
    private final int bCounter;

    public SPipesNodeFormatter(Model model, Map<String,String> ns, Map<String,Integer> inDegree,
                               Map<String,String> bnodeLabels, int bCounter){
        this.model=model; this.ns=ns; this.inDegree=inDegree; this.bnodeLabels=bnodeLabels; this.bCounter=bCounter;
    }

    @Override
    public void formatURI(AWriter w, String uriStr){
        String abbr=null; for(var e:ns.entrySet()) if(uriStr.startsWith(e.getValue())) abbr=e.getKey()+":"+uriStr.substring(e.getValue().length());
        w.print(abbr!=null?abbr:"<"+uriStr+">");
    }

    @Override
    public void formatBNode(AWriter w, String label){
        if(bnodeLabels.containsKey(label)){ w.print(bnodeLabels.get(label)); return; }
        int deg = inDegree.getOrDefault(label,0);
        if(deg<=1) { formatBNodeAsPropertyList(w, NodeFactory.createBlankNode(label), new HashSet<>()); return; }
        w.print("_:b"+label);
    }

    @Override
    public void formatLitString(AWriter w, String lex) { w.print("\""+escape(lex)+"\""); }
    @Override
    public void formatLitLang(AWriter w, String lex, String lang){ w.print("\""+escape(lex)+"\"@"+lang); }
    @Override
    public void formatLitDT(AWriter w, String lex, String dt){ w.print("\""+escape(lex)+"\"^^"); formatURI(w,dt); }
    @Override
    public void formatVar(AWriter w, String name){ w.print("?"+name); }

    public void formatNodeWithPath(AWriter w, Node node, Set<Node> path){
        if(node.isBlank()){ if(bnodeLabels.containsKey(node.getBlankNodeLabel())){w.print(bnodeLabels.get(node.getBlankNodeLabel())); return;}
            int deg = inDegree.getOrDefault(node.getBlankNodeLabel(),0);
            if(deg<=1){ formatBNodeAsPropertyList(w,node,path); return; }
            w.print("_:"+node.getBlankNodeLabel()); return;
        }
        format(w,node);
    }

    public void formatBNodeAsPropertyList(AWriter w, Node blank, Set<Node> path){
        if(!path.add(blank)){ w.print("_:"+blank.getBlankNodeLabel()); return;}
        List<Triple> props = new ArrayList<>();
        model.getGraph().find(blank,Node.ANY,Node.ANY).forEachRemaining(props::add);
        if(props.isEmpty()){ w.print("[]"); path.remove(blank); return;}
        w.print("[ ");
        props.sort(Comparator
                .comparing(Triple::getPredicate, PREDICATE_ORDER)
                .thenComparing(t -> t.getObject().toString()));
        for(Triple t:props){ Node p=t.getPredicate(),o=t.getObject();
            if(org.apache.jena.vocabulary.RDF.type.asNode().equals(p)) w.print("a"); else format(w,p); w.print(" ");
            formatNodeWithPath(w,o,path); w.print(" ; ");
        }
        w.print("]"); path.remove(blank);
    }

    private String escape(String s){
        return s.replace("\\","\\\\").replace("\"","\\\"");
    }

    private final Comparator<Node> PREDICATE_ORDER = (p1, p2) -> {
        if (p1.equals(p2)) return 0;
        Node rdfType = RDF.type.asNode();
        if (rdfType.equals(p1)) return -1;
        if (rdfType.equals(p2)) return 1;
        String u1 = p1.isURI() ? p1.getURI() : p1.toString();
        String u2 = p2.isURI() ? p2.getURI() : p2.toString();
        return u1.compareTo(u2);
    };

    @Override
    public void formatLiteral(AWriter w, Node n) {
        String lex = n.getLiteralLexicalForm();
        String lang = n.getLiteralLanguage();
        RDFDatatype dt = n.getLiteralDatatype();

        if (lang != null && !lang.isEmpty()) {
            w.print(formatLiteralString(lex) + "@" + lang);
        } else if (dt == null || dt.equals(XSDDatatype.XSDstring)) {
            w.print(formatLiteralString(lex));
        } else {
            w.print(formatLiteralString(lex) + "^^");
            formatURI(w, dt.getURI());
        }
    }

    private String formatLiteralString(String value) {
        boolean multiline = value.contains("\n") || value.contains("\r");
        String escaped = escapeString(value, multiline);
        return multiline ? "\"\"\"" + escaped + "\"\"\"" : "\"" + escaped + "\"";
    }

    private String escapeString(String s, boolean multiline) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\': b.append("\\\\"); break;
                case '"':
                    if (!multiline) b.append("\\\"");
                    else b.append('"');
                    break;
                case '\n': b.append(multiline ? "\n" : "\\n"); break;
                case '\r': b.append(multiline ? "\r" : "\\r"); break;
                case '\t': b.append("\\t"); break;
                case '\b': b.append("\\b"); break;
                case '\f': b.append("\\f"); break;
                default:
                    if (c < 0x20) b.append(String.format("\\u%04X", (int)c));
                    else b.append(c);
            }
        }
        return b.toString();
    }
}
