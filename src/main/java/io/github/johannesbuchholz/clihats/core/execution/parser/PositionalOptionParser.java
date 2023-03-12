package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.AbstractOptionParser;
import io.github.johannesbuchholz.clihats.core.execution.ValueMapper;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * These parsers are always parsed during the last parsing round and parse the remaining argument directly
 * from their respective position.
 */
public class PositionalOptionParser<T> extends AbstractOptionParser<T> {

    private final int position;
    private final ValueMapper<T> valueMapper;
    private final String description;
    private final boolean required;
    private final Supplier<String> defaultSupplier;

    private final String nameSubstitute;

    protected static PositionalOptionParser<String> at(int position) {
        return new PositionalOptionParser<>(position, () -> null, false, stringValue -> stringValue, "");
    }

    private PositionalOptionParser(int position, Supplier<String> defaultSupplier, boolean required, ValueMapper<T> valueMapper, String description) {
        if (position < 0) {
            throw new IllegalArgumentException("position needs to be non-negative but was " + position);
        }
        this.position = position;
        this.valueMapper = Objects.requireNonNull(valueMapper);
        this.description = Objects.requireNonNullElse(description, "").trim();
        this.required = required;
        this.defaultSupplier = Objects.requireNonNull(defaultSupplier);

        nameSubstitute = "position " + position;
    }

    /**
     * Returns a new PositionalArgument with this objects position and the given mapper.
     */
    public <X> PositionalOptionParser<X> withMapper(ValueMapper<X> mapper) {
        return new PositionalOptionParser<>(position, defaultSupplier, required, mapper, description);
    }

    public PositionalOptionParser<T> withDescription(String description) {
        return new PositionalOptionParser<>(position, defaultSupplier, required, valueMapper, description);
    }

    public PositionalOptionParser<T> isRequired(boolean required) {
        return new PositionalOptionParser<>(position, defaultSupplier, required, valueMapper, description);
    }

    public PositionalOptionParser<T> withDefault(String defaultValue) {
        return new PositionalOptionParser<>(position, () -> defaultValue, required, valueMapper, description);
    }

    public PositionalOptionParser<T> withDefault(Supplier<String> inputSupplier) {
        return new PositionalOptionParser<>(position, inputSupplier, required, valueMapper, description);
    }

    @Override
    public String[] getNames() {
        return new String[] {nameSubstitute};
    }

    @Override
    protected OptionParsingResult parse(List<String> inputArgs) {
        if (inputArgs.size() > position) {
            String stringValue = inputArgs.get(position);
            return createMappedResult(stringValue, valueMapper, stringValue);
        }
        // here if too few arguments are left
        if (required)
            return OptionParsingResult.notFound();
        else
            return createMappedResult(defaultSupplier.get(), valueMapper);
    }

    @Override
    protected OptionHelpContent getHelpContent() {
        return new OptionHelpContent(
                "<positional>",
                new String[] {},
                new String[]{Integer.toString(position), required ? "required" : null},
                description
        );
    }

    @Override
    protected int getParsingOrderWeight() {
        return Integer.MAX_VALUE;
    }

}
