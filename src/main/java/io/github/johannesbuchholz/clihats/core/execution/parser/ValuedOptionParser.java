package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.ArgumentParsingResult;
import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import io.github.johannesbuchholz.clihats.core.execution.ParserHelpContent;
import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.MissingValueException;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parses named arguments with user input values like
 * <p>-a 25, --my-argument 42, --stp some-string</p>
 *
 * @param <T> the type this parser returns.
 */
public class ValuedOptionParser<T> extends AbstractOptionParser<T> {

    /*
    If true, this parser expects values to be separated by a blank ' ' character after the occurrence of this option.
     */
    private final boolean required;
    private final ValueMapper<T> valueMapper;
    private final String description;
    private final Supplier<String> defaultSupplier;

    protected static ValuedOptionParser<String> forName(String name, String... names) {
        Set<OptionParserName> optionNames = Stream.concat(Stream.of(name), Stream.of(names))
                .map(OptionParserName::of)
                .collect(Collectors.toSet());
        return new ValuedOptionParser<>(optionNames, false, () -> null, s -> s, null);
    }

    private ValuedOptionParser(Set<OptionParserName> names, boolean required, Supplier<String> defaultSupplier, ValueMapper<T> valueMapper, String description) {
        super(names);
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

    public ValuedOptionParser<T> withDefault(Supplier<String> defaultSupplier) {
        return new ValuedOptionParser<>(names, required, Objects.requireNonNull(defaultSupplier), valueMapper, description);
    }

    public ValuedOptionParser<T> withRequired(boolean required) {
        return new ValuedOptionParser<>(names, required, defaultSupplier, valueMapper, description);
    }

    public ValuedOptionParser<T> withDescription(String description) {
        return new ValuedOptionParser<>(names, required, defaultSupplier, valueMapper, description);
    }

    @Override
    public ArgumentParsingResult<T> parse(InputArgument[] inputArgs, int index) throws ArgumentParsingException {
        if (inputArgs.length < index)
            throw new IllegalArgumentException("Index " + index + " is out of bounds for argument array of length " + inputArgs.length);
        InputArgument argToParse = Objects.requireNonNull(inputArgs[index], "Argument at index " + index + " is null");
        Optional<OptionParserName> match = names.stream()
                .filter(name -> name.matches(argToParse))
                .findAny();

        OptionParserName matchingName;
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
        List<OptionParserName> primaryNames = new ArrayList<>();
        List<OptionParserName> secondaryNames = new ArrayList<>();
        names.forEach(name -> {
            if (name.isPOSIXConformOptionName())
                primaryNames.add(name);
            else
                secondaryNames.add(name);
        });
        // synopsis
        List<String> indicators = new ArrayList<>();
        if (required)
            indicators.add("required");
        return new ParserHelpContent(
                primaryNames.stream().sorted().map(OptionParserName::getValue).collect(Collectors.toList()),
                secondaryNames.stream().sorted().map(OptionParserName::getValue).collect(Collectors.toList()),
                indicators,
                description,
                getSynopsisSnippet(primaryNames, secondaryNames)
        );
    }

    private String getSynopsisSnippet(List<OptionParserName> primaryNames, List<OptionParserName> secondaryNames) {
        String synopsisSnippet;
        if (!primaryNames.isEmpty()) {
            synopsisSnippet = "-" + primaryNames.stream().sorted().map(OptionParserName::getValueWithoutPrefix).collect(Collectors.joining());
        } else {
            synopsisSnippet = secondaryNames.stream().sorted().map(OptionParserName::getValue).collect(Collectors.joining("|"));
        }
        synopsisSnippet += " <value>";
        if (!required)
            synopsisSnippet = "[" + synopsisSnippet + "]";
        return synopsisSnippet;
    }

}
