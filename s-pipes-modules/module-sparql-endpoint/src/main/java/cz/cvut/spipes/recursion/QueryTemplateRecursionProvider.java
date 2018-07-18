package cz.cvut.spipes.recursion;

public interface QueryTemplateRecursionProvider extends ModuleRecursionProvider {
    String substituteQueryMarkers(int currentIteration, String queryStr);
}
