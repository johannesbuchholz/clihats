package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.ArgumentParsingResult;
import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import io.github.johannesbuchholz.clihats.core.execution.ParserHelpContent;
import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Parses arguments like {@code -t}, {@code --option-activated}, {@code --blubb} without an additional value.</p>
 * <p>Returns {@code null} or the specified default value if this argument is not among the given input arguments.
 * Otherwise, returns the stored flag value. The default flag value is the empty string.</p>
 * @param <T> the type this parser returns.
 */
public class FlagOptionParser<T> extends AbstractOptionParser<T> {

    private final String flagValue;
    private final String defaultValue;
    private final ValueMapper<T> valueMapper;
    private final String description;

    protected static FlagOptionParser<String> forName(String name, String... names) {
        Set<OptionParserName> optionNames = Stream.concat(Stream.of(name), Stream.of(names))
                .map(OptionParserName::of)
                .collect(Collectors.toSet());
        return new FlagOptionParser<>(optionNames, "", null, s -> s, null);
    }

    private FlagOptionParser(Set<OptionParserName> names, String flagValue, String defaultValue, ValueMapper<T> valueMapper, String description) {
        super(names);
        this.flagValue = flagValue;
        this.defaultValue = defaultValue;
        this.valueMapper = valueMapper;
        this.description = description;
    }

    public FlagOptionParser<T> withFlagValue(String flagValue) {
        return new FlagOptionParser<>(names, flagValue, defaultValue, valueMapper, description);
    }

    public FlagOptionParser<T> withDefault(String defaultValue) {
        return new FlagOptionParser<>(names, flagValue, defaultValue, valueMapper, description);
    }

    public <X> FlagOptionParser<X> withMapper(ValueMapper<X> valueMapper) {
        return new FlagOptionParser<>(names, flagValue, defaultValue, Objects.requireNonNull(valueMapper), description);
    }

    public FlagOptionParser<T> withDescription(String description) {
        return new FlagOptionParser<>(names, flagValue, defaultValue, valueMapper, description);
    }

    @Override
    public ArgumentParsingResult<T> parse(InputArgument[] inputArgs, int index) throws ArgumentParsingException {
        if (inputArgs.length < index)
            throw new IllegalArgumentException("Index " + index + " is out of bounds for argument array of length " + inputArgs.length);
        InputArgument inputArgument = Objects.requireNonNull(inputArgs[index], "Argument at index " + index + " is null");
        Optional<OptionParserName> match = names.stream()
                .filter(optionName -> optionName.matches(inputArgument))
                .findAny();
        OptionParserName matchingName;
        if (match.isEmpty()) {
            return ArgumentParsingResult.empty();
        } else {
            matchingName = match.get();
        }

        if (matchingName.isPOSIXConformOptionName()) {
            InputArgument newArg = inputArgument.newWithout(matchingName.getValue().charAt(1));
            if (newArg.isOption())
                inputArgs[index] = newArg;
            else
                inputArgs[index] = null;
        } else {
            inputArgs[index] = null;
        }
        return ArgumentParsingResult.of(mapWithThrows(valueMapper, flagValue));
    }

    @Override
    public ArgumentParsingResult<T> defaultValue() throws ArgumentParsingException {
        return ArgumentParsingResult.of(mapWithThrows(valueMapper, defaultValue));
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
        return new ParserHelpContent(
                primaryNames.stream().sorted().map(OptionParserName::getValue).collect(Collectors.toList()),
                secondaryNames.stream().sorted().map(OptionParserName::getValue).collect(Collectors.toList()),
                List.of("flag"),
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
        return  "[" + synopsisSnippet + "]";
    }

}
