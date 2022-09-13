package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.modules.exception.NoMatchException;

import java.util.function.UnaryOperator;


public class TabularModuleUtils {
    /**
     * This method sets the value of the column and table schema variables (e.g. name, title, aboutUrl, ...)
     * <p> If the value from schema is provided we check if it matches the value from the input data.
     * else we set the variable through variableSetter. </p>
     * @param schemaBasedValue The value of the column/tableSchema variable from the input schema
     * @param dataBasedValue The value of the column/tableSchema variable from the input data
     * @param variableSetter The setter of the column/tableSchema variable
     * @param variableName The name of the column/tableSchema variable we want to set
     */
    public void setVariable(String schemaBasedValue, String dataBasedValue,
                                   UnaryOperator<String> variableSetter, String variableName) {
        if (schemaBasedValue != null){
            checkVariable(schemaBasedValue, dataBasedValue, variableName);
        }else {
            variableSetter.apply(dataBasedValue);
        }
    }

    private void checkVariable(String schemaBasedValue, String dataBasedValue, String variableName) {
        if (!schemaBasedValue.equals(dataBasedValue)) {
            throw new NoMatchException(
                    String.format("Schema field '%s' with value '%s' does not match value '%s' from the input data ",
                            variableName, schemaBasedValue, dataBasedValue));
        }
    }
}
