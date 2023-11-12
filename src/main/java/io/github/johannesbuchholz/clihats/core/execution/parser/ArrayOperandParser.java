package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.ArgumentParsingResult;
import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import io.github.johannesbuchholz.clihats.core.execution.ParserHelpContent;
import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.ValueMappingException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Parses all remaining arguments into an array starting from a specific index.
 */
public class ArrayOperandParser<T> extends AbstractOperandParser<T[]> {

    private final Class<T> type;
    private final ValueMapper<T> valueMapper;
    private final String description;
    private final boolean required;
    private final Supplier<String[]> defaultSupplier;
    private final String displayName;

    protected static ArrayOperandParser<String> at(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index needs to be non-negative but was " + index);
        }
        return new ArrayOperandParser<>(index, String.class, () -> null, false, stringValue -> stringValue, "", null);
    }

    private ArrayOperandParser(int index, Class<T> type, Supplier<String[]> defaultSupplier, boolean required, ValueMapper<T> valueMapper, String description, String displayName) {
        super(index);
        this.type = type;
        this.valueMapper = valueMapper;
        this.description =description;
        this.required = required;
        this.defaultSupplier = defaultSupplier;
        this.displayName = displayName;
    }

    /**
     * Returns a new PositionalArgument with this objects position and the given mapper.
     */
    public <X> ArrayOperandParser<X> withMapper(ValueMapper<X> mapper, Class<X> type) {
        return new ArrayOperandParser<>(index, type, defaultSupplier, required, Objects.requireNonNull(mapper), description, displayName);
    }

    public ArrayOperandParser<T> withDescription(String description) {
        return new ArrayOperandParser<>(index, type, defaultSupplier, required, valueMapper,  description, displayName);
    }

    public ArrayOperandParser<T> withRequired(boolean required) {
        return new ArrayOperandParser<>(index, type, defaultSupplier, required, valueMapper, description, displayName);
    }

    public ArrayOperandParser<T> withDefault(String[] defaultValue) {
        return new ArrayOperandParser<>(index, type, () -> defaultValue, required, valueMapper, description, displayName);
    }

    public ArrayOperandParser<T> withDefault(Supplier<String[]> defaultSupplier) {
        return new ArrayOperandParser<>(index, type, Objects.requireNonNull(defaultSupplier), required, valueMapper, description, displayName);
    }

    public ArrayOperandParser<T> withDisplayName(String displayName) {
        return new ArrayOperandParser<>(index, type, Objects.requireNonNull(defaultSupplier), required, valueMapper, description, displayName);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public ArgumentParsingResult<T[]> parse(InputArgument[] inputArgs, int index) throws ArgumentParsingException {
        if (inputArgs.length < index)
            throw new IllegalArgumentException("Index " + index + " is out of bounds for argument array of length " + inputArgs.length);
        if (this.index == index) {
            String[] values = new String[inputArgs.length - index];
            for (int i = index; i < inputArgs.length; i++) {
                values[i - index] = Objects.requireNonNull(inputArgs[i], "Argument at " + i + " is null").getValue();
                inputArgs[i] = null;
            }
            return ArgumentParsingResult.of(mapValues(values));
        }
        // here if not found
        return ArgumentParsingResult.empty();
    }

    @Override
    public ArgumentParsingResult<T[]> defaultValue() throws ArgumentParsingException {
        if (required)
            return ArgumentParsingResult.empty();

        String[] defaultStringValue;
        try {
            defaultStringValue = defaultSupplier.get();
        } catch (Exception e) {
            throw new ArgumentParsingException(e);
        }
        return ArgumentParsingResult.of(mapValues(defaultStringValue));
    }

    @SuppressWarnings("unchecked")
    private T[] mapValues(String[] values) throws ValueMappingException {
        if (values == null)
            return null;
        T[] results = (T[]) Array.newInstance(type, values.length);
        for (int i = 0; i < values.length; i++) {
            String value = Objects.requireNonNull(values[i], "Value at index " + i + " is null");
            results[i] = mapWithThrows(value);
        }
        return results;
    }

    private T mapWithThrows(String stringValue) throws ValueMappingException {
        if (stringValue == null)
            return null;
        try {
            return valueMapper.map(stringValue);
        } catch (Exception e) {
            throw new ValueMappingException(this, e);
        }
    }

    @Override
    public ParserHelpContent getHelpContent() {
        List<String> additionalInfo = new ArrayList<>();
        String displayNameToShow = Objects.requireNonNullElseGet(displayName, () -> "OPERAND" + index);
        String synopsisSnippet = displayNameToShow;
        if (required) {
            additionalInfo.add("required");
        } else {
            synopsisSnippet = "[" + synopsisSnippet + "]";
        }
        return new ParserHelpContent(List.of(displayNameToShow), List.of(), additionalInfo, description, synopsisSnippet);
    }

}
