package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.ArgumentParsingResult;
import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.MissingValueException;

import java.util.*;
import java.util.function.Supplier;

/**
 * Parses named arguments with user input values like
 * <p>-a 25, --my-argument 42, --stp some-string</p>
 *
 * @param <T> the type this parser returns.
 */
public class ValuedOptionParser<T> extends AbstractOptionParser<T> {

    private final Set<OptionName> names;
    /*
    If true, this parser expects values to be separated by a blank ' ' character after the occurrence of this option.
     */
    private final boolean required;
    private final ValueMapper<T> valueMapper;
    private final String description;
    private final Supplier<String> defaultSupplier;

    protected static ValuedOptionParser<String> forName(String name, String... names) {
        return new ValuedOptionParser<>(collectAsOptionNamesFrom(name, names), false, () -> null, s -> s, null);
    }

    private ValuedOptionParser(Set<OptionName> names, boolean required, Supplier<String> defaultSupplier, ValueMapper<T> valueMapper, String description) {
        this.names = names;
        this.required = required;
        this.valueMapper = valueMapper;
        this.description = description;
        this.defaultSupplier = defaultSupplier;
    }

    // builder like methods

    public <X> ValuedOptionParser<X> withMapper(ValueMapper<X> valueMapper) {
        return new ValuedOptionParser<>(names, required, defaultSupplier, Objects.requireNonNull(valueMapper), description);
    }

    public ValuedOptionParser<T> withDefault(String defaultValue) {
        return new ValuedOptionParser<>(names, required, () -> defaultValue, valueMapper, description);
    }

    public ValuedOptionParser<T> withDefault(Supplier<String> inputSupplier) {
        return new ValuedOptionParser<>(names, required, Objects.requireNonNull(inputSupplier), valueMapper, description);
    }

    public ValuedOptionParser<T> withRequired(boolean required) {
        return new ValuedOptionParser<>(names, required, defaultSupplier, valueMapper, description);
    }

    public ValuedOptionParser<T> withDescription(String description) {
        return new ValuedOptionParser<>(names, required, defaultSupplier, valueMapper, Objects.requireNonNullElse(description, "").trim());
    }

    @Override
    public Set<OptionName> getNames() {
        return names;
    }

    @Override
    public ArgumentParsingResult<T> parse(InputArgument[] inputArgs, int index) throws ArgumentParsingException {
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
        ArgumentParsingResult<T> result;
        if (index + 1 < inputArgs.length) {
            InputArgument optionValue = inputArgs[index + 1];
            if (optionValue == null)
                throw new MissingValueException(this);
            String extractedStringValue = optionValue.getValue();
            inputArgs[index + 1] = null;
            result = ArgumentParsingResult.of(mapWithThrows(valueMapper, extractedStringValue));
        } else {
            throw new MissingValueException(this);
        }
        return result;
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
        List<String> primaryNames = new ArrayList<>();
        List<String> secondaryNames = new ArrayList<>();
        names.forEach(name -> {
            if (name.isPOSIXConformOptionName())
                primaryNames.add(name.getValue());
            else
                secondaryNames.add(name.getValue());
        });
        Collections.sort(primaryNames);
        Collections.sort(secondaryNames);
        List<String> indicators = new ArrayList<>();
        if (required)
            indicators.add("required");
        return new ParserHelpContent(primaryNames, secondaryNames, indicators, description);
    }

}
