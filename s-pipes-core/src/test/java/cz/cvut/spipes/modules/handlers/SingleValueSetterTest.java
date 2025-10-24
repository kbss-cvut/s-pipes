package cz.cvut.spipes.modules.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class SingleValueSetterTest {

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
    void setValueWhenPublicField() {
        SingleValueSetter<String> publicSingleValueSetter = new SingleValueSetter<>(testField, testBean);
        publicSingleValueSetter.setValue("newValue");

        assertEquals("newValue", testBean.publicField);
    }

    @Test
    void setValueWhenPrivateField() throws NoSuchFieldException {
        Field privateField = TestBean.class.getDeclaredField("privateField");
        privateField.setAccessible(true);
        SingleValueSetter<String> privateSingleValueSetter = new SingleValueSetter<>(privateField, testBean);

        privateSingleValueSetter.setValue("newPrivateValue");

        assertEquals("newPrivateValue", testBean.privateField);
    }

    @Test
    void setValueWhenPrivateFieldWhenInitiallyNotAccessible() throws NoSuchFieldException {
        Field privateField = TestBean.class.getDeclaredField("privateField");
        SingleValueSetter<String> privateSingleValueSetter = new SingleValueSetter<>(privateField, testBean);

        privateSingleValueSetter.setValue("newPrivateValue");

        assertEquals("newPrivateValue", testBean.privateField);
    }


    @Test
    void setValueWhenFieldWithDifferentTypeThrowsException() throws NoSuchFieldException {
        Field intField = TestBean.class.getDeclaredField("privateField");
        intField.setAccessible(true);
        SingleValueSetter<Integer> intSingleValueSetter = new SingleValueSetter<>(intField, testBean);

        assertThrows(IllegalArgumentException.class, () -> intSingleValueSetter.setValue(123));
    }
}
