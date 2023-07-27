package io.github.johannesbuchholz.clihats.core.execution;

public class ArgumentParsingResult {

    private final boolean isPresent;
    private final Object value;

    public static ArgumentParsingResult of(Object value) {
       return new ArgumentParsingResult(true, value);
    }

    public static ArgumentParsingResult empty() {
        return new ArgumentParsingResult(false, null);
    }

    private ArgumentParsingResult(boolean isPresent, Object value) {
        this.isPresent = isPresent;
        this.value = value;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public Object getValue() {
        return value;
    }

}
