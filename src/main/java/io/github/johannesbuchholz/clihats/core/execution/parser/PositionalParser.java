package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.exceptions.parsing.ValueExtractionException;
import io.github.johannesbuchholz.clihats.core.execution.ArgumentParsingResult;
import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import io.github.johannesbuchholz.clihats.core.execution.ParserHelpContent;
import io.github.johannesbuchholz.clihats.core.execution.ValueMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * These parsers are always parsed during the last parsing round and parse the remaining argument directly
 * from their respective position.
 */
public class PositionalParser<T> extends AbstractOperandParser {

    private final int position;
    private final ValueMapper<T> valueMapper;
    private final String description;
    private final boolean required;
    private final Supplier<String> defaultSupplier;

    protected static PositionalParser<String> at(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("position needs to be non-negative but was " + position);
        }
        return new PositionalParser<>(position, () -> null, false, stringValue -> stringValue, "");
    }

    private PositionalParser(int position, Supplier<String> defaultSupplier, boolean required, ValueMapper<T> valueMapper, String description) {
        this.position = position;
        this.valueMapper = valueMapper;
        this.description =description;
        this.required = required;
        this.defaultSupplier = defaultSupplier;
    }

    /**
     * Returns a new PositionalArgument with this objects position and the given mapper.
     */
    public <X> PositionalParser<X> withMapper(ValueMapper<X> mapper) {
        return new PositionalParser<>(position, defaultSupplier, required, Objects.requireNonNull(mapper), description);
    }

    public PositionalParser<T> withDescription(String description) {
        return new PositionalParser<>(position, defaultSupplier, required, valueMapper,  Objects.requireNonNullElse(description, "").trim());
    }

    public PositionalParser<T> withRequired(boolean required) {
        return new PositionalParser<>(position, defaultSupplier, required, valueMapper, description);
    }

    public PositionalParser<T> withDefault(String defaultValue) {
        return new PositionalParser<>(position, () -> defaultValue, required, valueMapper, description);
    }

    public PositionalParser<T> withDefault(Supplier<String> defaultSupplier) {
        return new PositionalParser<>(position, Objects.requireNonNull(defaultSupplier), required, valueMapper, description);
    }

    @Override
    protected int getPosition() {
        return position;
    }

    @Override
    protected ArgumentParsingResult parse(InputArgument[] inputArgs, int index) throws ValueExtractionException {
        if (inputArgs.length < index)
            throw new IllegalArgumentException("Index " + index + " is out of bounds for argument array of length " + inputArgs.length);
        if (position == index) {
            InputArgument inputArgument = Objects.requireNonNull(inputArgs[index], "Argument at index " + index + " is null");
            inputArgs[index] = null;
            return ArgumentParsingResult.of(valueMapper.mapWithThrows(inputArgument.getValue()));
        }
        // here if not found
        return ArgumentParsingResult.empty();
    }

    @Override
    protected ArgumentParsingResult defaultValue() throws ValueExtractionException {
        if (required)
            return ArgumentParsingResult.empty();

        String defaultStringValue;
        try {
             defaultStringValue = defaultSupplier.get();
        } catch (Exception e) {
            throw new ValueExtractionException(e);
        }
        return ArgumentParsingResult.of(valueMapper.mapWithThrows(defaultStringValue));
    }

    @Override
    protected ParserHelpContent getHelpContent() {
        List<String> indicators = new ArrayList<>();
        if (required)
            indicators.add("required");
        return new ParserHelpContent(List.of("< " + position + ">"), List.of(), indicators, description);
    }

}
