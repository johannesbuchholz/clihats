package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.exceptions.parsing.MissingValueException;
import io.github.johannesbuchholz.clihats.core.exceptions.parsing.ValueExtractionException;
import io.github.johannesbuchholz.clihats.core.execution.*;

import java.util.*;
import java.util.function.Supplier;

/**
 * Parses named arguments with user input values like
 * <p>-a 25, --my-argument 42, --stp some-string</p>
 *
 * @param <T> the type this parser returns.
 */
public class ValuedParser<T> extends AbstractOptionParser {

    private final Set<OptionName> names;
    /*
    If true, this parser expects values to be separated by a blank ' ' character after the occurrence of this option.
     */
    private final boolean required;
    private final ValueMapper<T> valueMapper;
    private final String description;
    private final Supplier<String> defaultSupplier;

    protected static ValuedParser<String> forName(String name, String... names) {
        return new ValuedParser<>(asOptionNames(name, names), false, () -> null, s -> s, null);
    }

    private ValuedParser(Set<OptionName> names, boolean required, Supplier<String> defaultSupplier, ValueMapper<T> valueMapper, String description) {
        this.names = names;
        this.required = required;
        this.valueMapper = valueMapper;
        this.description = description;
        this.defaultSupplier = defaultSupplier;
    }

    // builder like methods

    public <X> ValuedParser<X> withMapper(ValueMapper<X> valueMapper) {
        return new ValuedParser<>(names, required, defaultSupplier, Objects.requireNonNull(valueMapper), description);
    }

    public ValuedParser<T> withDefault(String defaultValue) {
        return new ValuedParser<>(names, required, () -> defaultValue, valueMapper, description);
    }

    public ValuedParser<T> withDefault(Supplier<String> inputSupplier) {
        return new ValuedParser<>(names, required, Objects.requireNonNull(inputSupplier), valueMapper, description);
    }

    public ValuedParser<T> withRequired(boolean required) {
        return new ValuedParser<>(names, required, defaultSupplier, valueMapper, description);
    }

    public ValuedParser<T> withDescription(String description) {
        return new ValuedParser<>(names, required, defaultSupplier, valueMapper, Objects.requireNonNullElse(description, "").trim());
    }

    @Override
    Set<OptionName> getNames() {
        return names;
    }

    @Override
    protected ArgumentParsingResult parse(InputArgument[] inputArgs, int index) throws ValueExtractionException {
        if (inputArgs.length < index)
            throw new IllegalArgumentException("Index " + index + " is out of bounds for argument array of length " + inputArgs.length);
        InputArgument argToParse = Objects.requireNonNull(inputArgs[index], "Argument at index " + index + " is null");
        Optional<OptionName> match = names.stream()
                .filter(name -> name.matches(argToParse))
                .findAny();

        OptionName matchingName;
        if (match.isEmpty())
            return ArgumentParsingResult.empty();
        else
            matchingName = match.get();

        // reduce matching argument
        if (matchingName.isPOSIXConformOptionName()) {
            InputArgument newArg = argToParse.newWithout(matchingName.getValue().charAt(1));
            if (newArg.isOption())
                inputArgs[index] = newArg;
            else
                inputArgs[index] = null;
        } else {
            inputArgs[index] = null;
        }

        // extract value
        ArgumentParsingResult result;
        if (index + 1 < inputArgs.length) {
            String extractedStringValue = inputArgs[index + 1].getValue();
            inputArgs[index + 1] = null;
            result = ArgumentParsingResult.of(valueMapper.mapWithThrows(extractedStringValue));
        } else {
            throw new MissingValueException(getDisplayName());
        }
        return result;
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
    protected Optional<String> getConflictMessage(AbstractParser other) {
        return Optional.empty();
    }

    @Override
    protected ParserHelpContent getHelpContent() {
        List<String> primaryNames = new ArrayList<>();
        List<String> secondaryNames = new ArrayList<>();
        names.forEach(name -> {
            if (name.isPOSIXConformOptionName())
                primaryNames.add(name.getValue());
            else
                secondaryNames.add(name.getValue());
        });
        List<String> indicators = new ArrayList<>();
        if (required)
            indicators.add("required");
        return new ParserHelpContent(primaryNames, secondaryNames, indicators, description);
    }

}
