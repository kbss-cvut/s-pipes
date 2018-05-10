package cz.cvut.sempipes.recursion;

public interface QueryTemplateRecursionProvider extends ModuleRecursionProvider {
    String substituteQueryMarkers(int currentIteration, String queryStr);
}
