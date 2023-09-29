package cz.cvut.spipes.modules.exception;

import cz.cvut.spipes.exception.SPipesException;
import cz.cvut.spipes.modules.ResourceFormat;

public class SpecificationNonComplianceException extends SPipesException {
    public SpecificationNonComplianceException(ResourceFormat format, int delimiter) {
        super("overwritten delimiter \""+ ((char) delimiter) + "\" is not complaint with resource format: " + format.getValue());
    }
}
