package cz.cvut.spipes.function;


import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

public class EncodePasswordTest {

    PasswordEncoder encoder = PasswordEncoderConfig.getEncoder();

    @Test
    public void execReturnsEncodedPassword() {

        EncodePassword EncodePassword = new EncodePassword();
        Node plainPasswordNode = NodeValue.makeNodeString("rawPassword").asNode();
        NodeValue encodedPasswordNodeValue = EncodePassword.exec(plainPasswordNode, null);

        String ahoj = encodedPasswordNodeValue.toString();
        System.out.println(ahoj);
        String encodedP = PasswordEncoderConfig.getEncoder().encode("rawPassword");
        encoder.matches("rawPassword", encodedP);

        assertTrue(encoder.matches("rawPassword", encodedPasswordNodeValue.asString()));
    }

}