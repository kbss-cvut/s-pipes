package cz.cvut.spipes.util;

import org.apache.commons.io.output.WriterOutputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WriterGraphRIOT;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.util.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class SPipesTurtleWriter implements WriterGraphRIOT {

    @Override
    public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        SPipesFormatter formatter = new SPipesFormatter(graph, prefixMap);
        formatter.writeTo(out);
        try {
            out.flush();
        } catch (IOException e) {
            throw new RiotException("Failed to flush output", e);
        }
    }

    @Override
    public void write(Writer writer, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        write(new WriterOutputStream(writer, StandardCharsets.UTF_8), graph, prefixMap, baseURI, context);
    }

    @Override
    public Lang getLang() {
        return CustomLangs.SPIPES_TURTLE;
    }
}
