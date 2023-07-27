package io.github.johannesbuchholz.clihats.core.exceptions.parsing;

public class ValuedOptionGroupingException extends ValueExtractionException {

    public ValuedOptionGroupingException(String argumentName, String illegalArgument) {
        super("Found option with option-value not placed at the end of the argument: " + argumentName + " in " + illegalArgument);
    }

}
