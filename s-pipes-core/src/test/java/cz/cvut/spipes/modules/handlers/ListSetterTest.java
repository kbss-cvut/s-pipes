package cz.cvut.spipes.modules.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListSetterTest {

    private TestBean testBean;
    private Field testField;
    private ListSetter<?> listSetter;

    public static class TestBean {
        public List<Object> values;
    }

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        testBean = new TestBean();
        testField = TestBean.class.getField("values");
        listSetter = new ListSetter<>(testField, testBean);
    }

    @Test
    void testAddValueWhenListIsNull() {
        assertNull(testBean.values);

        listSetter.addValue("testValue");

        assertNotNull(testBean.values);
        assertEquals(1, testBean.values.size());
        assertEquals("testValue", testBean.values.get(0));
    }

    @Test
    void testAddValueWhenListIsAlreadyInitialized() {
        testBean.values = new ArrayList<>();

        listSetter.addValue("testValue");

        assertEquals(1, testBean.values.size());
        assertEquals("testValue", testBean.values.get(0));
    }

    @Test
    void testAddListOfValues() {
        List<Object> valuesToAdd = Arrays.asList("value1", "value2", "value3");

        listSetter.addValue(valuesToAdd);

        assertNotNull(testBean.values);
        assertEquals(3, testBean.values.size());
        assertTrue(testBean.values.containsAll(valuesToAdd));
    }

    @Test
    void testAddValueWhenListIsNullAndValueIsList() {
        List<Object> valuesToAdd = Arrays.asList("value1", "value2");

        listSetter.addValue(valuesToAdd);

        assertNotNull(testBean.values);
        assertEquals(2, testBean.values.size());
        assertEquals("value1", testBean.values.get(0));
        assertEquals("value2", testBean.values.get(1));
    }

    @Test
    void testAddSingleValueToExistingList() {
        testBean.values = new ArrayList<>();
        testBean.values.add("initialValue");

        listSetter.addValue("newValue");

        assertEquals(1, testBean.values.size());
        assertEquals("newValue", testBean.values.get(0));
    }

    @Test
    void testAddListToExistingList() {
        testBean.values = new ArrayList<>();
        testBean.values.add("initialValue");

        List<Object> valuesToAdd = Arrays.asList("newValue1", "newValue2");
        listSetter.addValue(valuesToAdd);

        assertEquals(2, testBean.values.size());
        assertEquals("newValue1", testBean.values.get(0));
        assertEquals("newValue2", testBean.values.get(1));
    }
}
