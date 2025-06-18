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
        NodeValue rawPasswordNode = NodeValue.makeNodeString("rawPassword");
        NodeValue encodedPasswordNode = NodeValue.makeNodeString(encoder.encode("rawPassword"));

        NodeValue retVal = passwordEncoder.exec(rawPasswordNode, encodedPasswordNode);

        assertTrue(retVal.getBoolean());
    }

    @Test
    public void execReturnsFalseIfRawMatchesEncodedPassword() {

        MatchesPassword passwordEncoder = new MatchesPassword();
        NodeValue rawPasswordNode = NodeValue.makeNodeString("rawPassword");
        NodeValue encodedPasswordNode = NodeValue.makeNodeString(encoder.encode("DIFFERENT_Password"));

        NodeValue retVal = passwordEncoder.exec(rawPasswordNode, encodedPasswordNode);

        assertFalse(retVal.getBoolean());
    }
}