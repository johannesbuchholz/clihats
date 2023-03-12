package io.github.johannesbuchholz.clihats.core.execution.parser;

import java.util.List;

public class OptionParsingResult {

    private final boolean isFound;
    private final Object value;
    private final List<String> usedInputArguments;
    private final Throwable exception;

    /**
     * Indicates missing required argument.
     */
    public static OptionParsingResult notFound()  {
        return new OptionParsingResult(false, null, null);
    }

    /**
     * Indicates a found value. Either a default value if the actual argument was missing or the parsed value.
     * <p>
     *     The parsed value is allowed to be null.
     * </p>
     */
    public static OptionParsingResult found(Object parsedValue, String... usedArgs) {
        return new OptionParsingResult(true, parsedValue, null, usedArgs);
    }

    /**
     * Indicates an error during parsing.
     */
    public static OptionParsingResult error(Throwable encounteredException, String... usedArgs) {
        return new OptionParsingResult(false, null, encounteredException, usedArgs);
    }

    private OptionParsingResult(boolean isFound, Object value, Throwable exception, String... usedInputArguments) {
        this.isFound = isFound;
        this.value = value;
        this.exception = exception;
        this.usedInputArguments = List.of(usedInputArguments);
    }

    public boolean isFound() {
        return isFound;
    }

    public boolean isError() {
        return exception != null;
    }

    public Object getValue() {
        return value;
    }

    public List<String> getParsedInputArguments() {
        return usedInputArguments;
    }

    public Throwable getCause() {
        return exception;
    }
}
