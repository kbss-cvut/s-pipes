package cz.cvut.spipes.modules.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class FieldSetterTest {

    private TestBean testBean;
    private Field testField;

    public static class TestBean {
        public String publicField;
        private final String privateField;

        public TestBean(String privateField) {
            this.privateField = privateField;
        }
    }

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        testBean = new TestBean("initialValue");
        testField = TestBean.class.getField("publicField");
    }

    @Test
    void addValueWhenPublicField() {
        FieldSetter<String> publicFieldSetter = new FieldSetter<>(testField, testBean);
        publicFieldSetter.addValue("newValue");

        assertEquals("newValue", testBean.publicField);
    }

    @Test
    void addValueWhenPrivateField() throws NoSuchFieldException {
        Field privateField = TestBean.class.getDeclaredField("privateField");
        privateField.setAccessible(true);
        FieldSetter<String> privateFieldSetter = new FieldSetter<>(privateField, testBean);

        privateFieldSetter.addValue("newPrivateValue");

        assertEquals("newPrivateValue", testBean.privateField);
    }

    @Test
    void addValueWhenPrivateFieldWhenInitiallyNotAccessible() throws NoSuchFieldException {
        Field privateField = TestBean.class.getDeclaredField("privateField");
        FieldSetter<String> privateFieldSetter = new FieldSetter<>(privateField, testBean);

        privateFieldSetter.addValue("newPrivateValue");

        assertEquals("newPrivateValue", testBean.privateField);
    }


    @Test
    void addValueWhenFieldWithDifferentTypeThrowsException() throws NoSuchFieldException {
        Field intField = TestBean.class.getDeclaredField("privateField");
        intField.setAccessible(true);
        FieldSetter<Integer> intFieldSetter = new FieldSetter<>(intField, testBean);

        assertThrows(IllegalArgumentException.class, () -> intFieldSetter.addValue(123));
    }
}
