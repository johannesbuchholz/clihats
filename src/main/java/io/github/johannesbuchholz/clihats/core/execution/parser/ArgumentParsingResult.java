package io.github.johannesbuchholz.clihats.core.execution.parser;

public class ArgumentParsingResult<T> {

    private final boolean isPresent;
    // Can be null
    private final T value;

    public static <T> ArgumentParsingResult<T> of(T value) {
       return new ArgumentParsingResult<>(true, value);
    }

    public static <T> ArgumentParsingResult<T> empty() {
        return new ArgumentParsingResult<>(false, null);
    }

    private ArgumentParsingResult(boolean isPresent, T value) {
        this.isPresent = isPresent;
        this.value = value;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public T getValue() {
        return value;
    }

}
