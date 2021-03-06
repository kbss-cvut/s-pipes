package cz.cvut.spipes.function;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

public class MatchesPasswordTest {

    PasswordEncoder encoder = PasswordEncoderConfig.getEncoder();

    @Test
    public void execReturnsTrueIfRawMatchesEncodedPassword() {

        MatchesPassword passwordEncoder = new MatchesPassword();
        Node rawPasswordNode = NodeValue.makeNodeString("rawPassword").asNode();
        Node encodedPasswordNode = NodeValue.makeNodeString(encoder.encode("rawPassword")).asNode();

        NodeValue retVal = passwordEncoder.exec(rawPasswordNode, encodedPasswordNode,null);

        assertTrue(retVal.getBoolean());
    }

    @Test
    public void execReturnsFalseIfRawMatchesEncodedPassword() {

        MatchesPassword passwordEncoder = new MatchesPassword();
        Node rawPasswordNode = NodeValue.makeNodeString("rawPassword").asNode();
        Node encodedPasswordNode = NodeValue.makeNodeString(encoder.encode("DIFFERENT_Password")).asNode();

        NodeValue retVal = passwordEncoder.exec(rawPasswordNode, encodedPasswordNode,null);

        assertFalse(retVal.getBoolean());
    }
}