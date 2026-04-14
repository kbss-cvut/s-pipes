package cz.cvut.spipes.migration.v5;

import cz.cvut.spipes.spin.vocabulary.SP;
import org.apache.jena.rdf.model.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Step 5: Replace SPIN/SPIF function calls in sp:text / sh:select literals.
 *
 * Scans all sp:text and sh:select literal values for function calls using the spif: prefix
 * (e.g. spif:buildString(...), spif:cast(...)) or sp: prefix (e.g. sp:concat(...)).
 * These are TopBraid-specific or SPIN functions not available in standard Jena/SPARQL.
 * If a mapping exists for the function, replaces it.
 * If no mapping exists, throws a RuntimeException.
 */
public class ReplaceSpinFunctionCalls {

    private static final String SH_NS = "http://www.w3.org/ns/shacl#";
    private static final Property SH_select = ResourceFactory.createProperty(SH_NS + "select");

    private static final Pattern SPIN_FUNCTION_CALL = Pattern.compile("\\b(sp|spif):(\\w+)\\(");

    private final Map<String, String> replacements;

    public ReplaceSpinFunctionCalls() {
        this.replacements = new LinkedHashMap<>();
    }

    public ReplaceSpinFunctionCalls(Map<String, String> replacements) {
        this.replacements = new LinkedHashMap<>(replacements);
    }

    public void apply(Model model) {
        for (Property predicate : List.of(SP.text, SH_select)) {
            List<Statement> statements = model.listStatements(null, predicate, (RDFNode) null).toList();
            for (Statement stmt : statements) {
                if (!stmt.getObject().isLiteral()) {
                    continue;
                }
                String text = stmt.getLiteral().getLexicalForm();
                String replaced = replaceSpinFunctionCalls(model, stmt.getSubject(), predicate, text);
                if (!replaced.equals(text)) {
                    stmt.changeObject(replaced);
                }
            }
        }
    }

    private String replaceSpinFunctionCalls(Model model, Resource subject, Property predicate, String text) {
        Matcher matcher = SPIN_FUNCTION_CALL.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String prefix = matcher.group(1);
            String funcName = matcher.group(2);
            String prefixedCall = prefix + ":" + funcName;
            String replacement = replacements.get(prefixedCall);
            if (replacement == null) {
                throw new RuntimeException(
                    "Cannot migrate " + toPrefixedName(model, predicate) + " of resource "
                        + describeResource(model, subject)
                        + " because it contains unmapped function call '" + prefixedCall + "'."
                );
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement + "("));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String describeResource(Model model, Resource resource) {
        if (resource.isURIResource()) {
            return toPrefixedName(model, resource);
        }
        List<Statement> refs = model.listStatements(null, null, resource).toList();
        if (!refs.isEmpty()) {
            Statement ref = refs.get(0);
            return "(referenced by " + toPrefixedName(model, ref.getSubject())
                + " via " + toPrefixedName(model, ref.getPredicate()) + ")";
        }
        return "(anonymous blank node)";
    }

    private String toPrefixedName(Model model, Resource resource) {
        String uri = resource.getURI();
        String qname = model.qnameFor(uri);
        if (qname != null) {
            return qname;
        }
        return "<" + uri + ">";
    }
}
