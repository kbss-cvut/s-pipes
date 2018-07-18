package cz.cvut.spipes.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.apache.jena.rdf.model.ResourceFactory;
import static org.hamcrest.CoreMatchers.containsString;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class VariablesBindingTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void restrictToReturnsOnlyListedVariables() {
        final VariablesBinding vb = new VariablesBinding();
        vb.add("var1", ResourceFactory.createStringLiteral("value1"));
        vb.add("var2", ResourceFactory.createStringLiteral("value2"));
        vb.add("var3", ResourceFactory.createStringLiteral("value3"));
        vb.add("var4", ResourceFactory.createStringLiteral("value4"));
        VariablesBinding newVB = vb.restrictTo("var2", "var4");
        assertEquals(getSize(newVB), 2);
        assertNotNull(newVB.asQuerySolution().get("var2"));
        assertNotNull(newVB.asQuerySolution().get("var4"));
        assertEquals(newVB.asQuerySolution().get("var2").toString(), "value2");
        assertEquals(newVB.asQuerySolution().get("var4").toString(), "value4");
    }

    @Test
    public void loadMultipleBindings() throws Exception {
        final VariablesBinding vb = new VariablesBinding();
        final InputStream is = this.getClass().getResourceAsStream("/engine/variables-binding-test-1.ttl");
        thrown.expect(IOException.class);
        thrown.expectMessage(containsString("1 was expected"));
        vb.load(is, "TURTLE");
    }

    @Test
    public void loadNoBinding() throws Exception {
        final VariablesBinding vb = new VariablesBinding();
        final InputStream is = this.getClass().getResourceAsStream("/engine/variables-binding-test-2.ttl");
        thrown.expect(IOException.class);
        thrown.expectMessage(containsString("1 was expected"));
        vb.load(is, "TURTLE");
    }

    @Test
    public void loadCorrectBindingWithTwoVariables() throws Exception {
        final VariablesBinding vb = new VariablesBinding();
        final InputStream is = this.getClass().getResourceAsStream("/engine/variables-binding-test-3.ttl");

        vb.load(is, "TURTLE");

        Assert.assertEquals(iteratorToStream(vb.asQuerySolution().varNames()).count(),2);
    }

    @Test
    public void saveAndLoadBinding() throws Exception {
        final VariablesBinding vb = new VariablesBinding();
        vb.add("x", ResourceFactory.createResource("http://example.org/test-resource"));
        vb.add("y", ResourceFactory.createPlainLiteral("plain literal"));
        vb.add("z", ResourceFactory.createPlainLiteral("plain literal 2"));

        final File f = File.createTempFile("variables-binding","ttl");

        vb.save(new FileOutputStream(f),"TURTLE");

        final VariablesBinding vb2 = new VariablesBinding();
        vb2.load(new FileInputStream(f),"TURTLE");

        Assert.assertEquals(iteratorToStream(vb.asQuerySolution().varNames()).count(),3);

        Assert.assertEquals(vb.asQuerySolution().get("x").asResource().getURI(),"http://example.org/test-resource");
        Assert.assertEquals(vb.asQuerySolution().get("y").asLiteral().getString(),"plain literal");
        Assert.assertEquals(vb.asQuerySolution().get("z").asLiteral().getString(),"plain literal 2");
    }

    private <T> Stream<T> iteratorToStream(final Iterator<T> iterator) {
        int characteristics = Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, characteristics);
        return StreamSupport.stream(spliterator, false);
    }

    private int getSize(VariablesBinding newVB) {
        final Integer[] countRef = {0};
        newVB.getVarNames().forEachRemaining(
            n -> { countRef[0]++; }
        );
        return countRef[0];
    }

}