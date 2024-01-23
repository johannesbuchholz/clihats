package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.ArgumentParsingResult;
import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import io.github.johannesbuchholz.clihats.core.execution.ParserHelpContent;
import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * These parsers are always parsed during the last parsing round and parse the remaining argument directly
 * from their respective position.
 */
public class OperandParser<T> extends AbstractOperandParser<T> {

    private final ValueMapper<T> valueMapper;
    private final String description;
    private final boolean required;
    private final Supplier<String> defaultSupplier;
    private final String displayName;

    protected static OperandParser<String> at(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index needs to be non-negative but was " + index);
        }
        return new OperandParser<>(index, () -> null, false, stringValue -> stringValue, "", null);
    }

    private OperandParser(int index, Supplier<String> defaultSupplier, boolean required, ValueMapper<T> valueMapper, String description, String displayName) {
        super(index);
        this.valueMapper = valueMapper;
        this.description =description;
        this.required = required;
        this.defaultSupplier = defaultSupplier;
        this.displayName = displayName;
    }

    /**
     * Returns a new PositionalArgument with this objects position and the given mapper.
     */
    public <X> OperandParser<X> withMapper(ValueMapper<X> mapper) {
        return new OperandParser<>(index, defaultSupplier, required, Objects.requireNonNull(mapper), description, displayName);
    }

    public OperandParser<T> withDescription(String description) {
        return new OperandParser<>(index, defaultSupplier, required, valueMapper,  description, displayName);
    }

    public OperandParser<T> withRequired(boolean required) {
        return new OperandParser<>(index, defaultSupplier, required, valueMapper, description, displayName);
    }

    public OperandParser<T> withDefault(String defaultValue) {
        return new OperandParser<>(index, () -> defaultValue, required, valueMapper, description, displayName);
    }

    public OperandParser<T> withDefault(Supplier<String> defaultSupplier) {
        return new OperandParser<>(index, Objects.requireNonNull(defaultSupplier), required, valueMapper, description, displayName);
    }

    public OperandParser<T> withDisplayName(String displayName) {
        return new OperandParser<>(index, Objects.requireNonNull(defaultSupplier), required, valueMapper, description, displayName);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public ArgumentParsingResult<T> parse(InputArgument[] inputArgs, int index) throws ArgumentParsingException {
        if (inputArgs.length < index)
            throw new IllegalArgumentException("Index " + index + " is out of bounds for argument array of length " + inputArgs.length);
        if (this.index == index) {
            InputArgument inputArgument = Objects.requireNonNull(inputArgs[index], "Argument at index " + index + " is null");
            inputArgs[index] = null;
            return ArgumentParsingResult.of(mapWithThrows(valueMapper, inputArgument.getValue()));
        }
        // here if not found
        return ArgumentParsingResult.empty();
    }

    @Override
    public ArgumentParsingResult<T> defaultValue() throws ArgumentParsingException {
        if (required)
            return ArgumentParsingResult.empty();

        String defaultStringValue;
        try {
             defaultStringValue = defaultSupplier.get();
        } catch (Exception e) {
            throw new ArgumentParsingException(e);
        }
        return ArgumentParsingResult.of(mapWithThrows(valueMapper, defaultStringValue));
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

    @Override
    public String toString() {
        return Optional.ofNullable(displayName).orElse(super.toString());
    }

}
