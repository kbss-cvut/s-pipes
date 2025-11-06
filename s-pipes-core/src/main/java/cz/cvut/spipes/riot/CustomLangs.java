package cz.cvut.spipes.riot;

import org.apache.jena.riot.*;

public class CustomLangs {
    public static final Lang SPIPES_TURTLE = LangBuilder.create("SPIPES-TURTLE", "text/spipes+turtle").build();
    public static final RDFFormat SPIPES_FORMAT = new RDFFormat(SPIPES_TURTLE);

    static {
        RDFWriterRegistry.register(SPIPES_FORMAT, (WriterGraphRIOTFactory) (lang) -> new SPipesTurtleWriter());
    }
}