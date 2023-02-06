package cz.cvut.spipes.modules.textAnalysis;

import cz.cvut.spipes.constants.Constants;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;


public class Extraction {

    private Map<String, String> prefixes = new HashMap<>();

    public Map<String, List<Element>> getTermOccurrences(Element rootElement) {
        Map<String, List<Element>> annotatedElements = new HashMap<>();
        final Elements elements = rootElement.getElementsByAttribute(Constants.RDFa.ABOUT);
        for (Element element : elements) {
            if (isNotTermOccurrence(element)) {
                continue;
            }
            annotatedElements.computeIfAbsent(element.attr(Constants.RDFa.ABOUT), key -> new ArrayList<>())
                    .add(element);
        }
        return annotatedElements;
    }

    private boolean isNotTermOccurrence(Element rdfaElem) {
        if (!rdfaElem.hasAttr(Constants.RDFa.RESOURCE) && !rdfaElem.hasAttr(Constants.RDFa.CONTENT)) {
            return true;
        }
        final String typesString = rdfaElem.attr(Constants.RDFa.TYPE);
        final String[] types = typesString.split(" ");
        for (String type : types) {
            final String fullType = fullIri(type);
            if (fullType.equals(Constants.VYSKYT_TERMU)) {
                return false;
            }
        }
        return true;
    }

    private String fullIri(String possiblyPrefixed) {
        possiblyPrefixed = possiblyPrefixed.trim();
        final int colonIndex = possiblyPrefixed.indexOf(':');
        if (colonIndex == -1) {
            return possiblyPrefixed;
        }
        final String prefix = possiblyPrefixed.substring(0, colonIndex);
        if (!prefixes.containsKey(prefix)) {
            return possiblyPrefixed;
        }
        final String localName = possiblyPrefixed.substring(colonIndex + 1);
        return prefixes.get(prefix) + localName;
    }

    public void addPrefix(String prefix, String value) {
        prefixes.put(prefix, value);
    }

    public Map<String, String> getPrefixes() {
        return prefixes;
    }
}