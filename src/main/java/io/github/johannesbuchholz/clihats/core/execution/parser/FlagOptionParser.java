package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.ArgumentParsingResult;
import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import io.github.johannesbuchholz.clihats.core.execution.ParserHelpContent;
import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Parses arguments like {@code -t}, {@code --option-activated}, {@code --blubb} without an additional value.</p>
 * <p>Returns {@code null} or the specified default value if this argument is not among the given input arguments.
 * Otherwise, returns the stored flag value. The default flag value is the empty string.</p>
 * @param <T> the type this parser returns.
 */
public class FlagOptionParser<T> extends AbstractOptionParser<T> {

    private final Set<OptionName> names;
    private final String flagValue;
    private final String defaultValue;
    private final ValueMapper<T> valueMapper;
    private final String description;

    protected static FlagOptionParser<String> forName(String name, String... names) {
        return new FlagOptionParser<>(collectAsOptionNamesFrom(name, names), "", null, s -> s, null);
    }

    private FlagOptionParser(Set<OptionName> names, String flagValue, String defaultValue, ValueMapper<T> valueMapper, String description) {
        this.names = names;
        this.flagValue = flagValue;
        this.defaultValue = defaultValue;
        this.valueMapper = valueMapper;
        this.description = Objects.requireNonNullElse(description, "").trim();
    }

    public FlagOptionParser<T> withFlagValue(String flagValue) {
        return new FlagOptionParser<>(names, Objects.requireNonNull(flagValue), defaultValue, valueMapper, description);
    }

    public FlagOptionParser<T> withDefault(String defaultValue) {
        return new FlagOptionParser<>(names, flagValue, defaultValue, valueMapper, description);
    }

    public <X> FlagOptionParser<X> withMapper(ValueMapper<X> valueMapper) {
        return new FlagOptionParser<>(names, flagValue, defaultValue, Objects.requireNonNull(valueMapper), description);
    }

    public FlagOptionParser<T> withDescription(String description) {
        return new FlagOptionParser<>(names, flagValue, defaultValue, valueMapper, Objects.requireNonNullElse(description, "").trim());
    }

    @Override
    public Set<OptionName> getNames() {
        return names;
    }

    @Override
    public ArgumentParsingResult<T> parse(InputArgument[] inputArgs, int index) throws ArgumentParsingException {
        if (inputArgs.length < index)
            throw new IllegalArgumentException("Index " + index + " is out of bounds for argument array of length " + inputArgs.length);
        InputArgument inputArgument = Objects.requireNonNull(inputArgs[index], "Argument at index " + index + " is null");
        Optional<OptionName> match = names.stream()
                .filter(optionName -> optionName.matches(inputArgument))
                .findAny();
        OptionName matchingName;
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
        List<OptionName> primaryNames = new ArrayList<>();
        List<OptionName> secondaryNames = new ArrayList<>();
        names.forEach(name -> {
            if (name.isPOSIXConformOptionName())
                primaryNames.add(name);
            else
                secondaryNames.add(name);
        });
        Collections.sort(primaryNames);
        Collections.sort(secondaryNames);
        return new ParserHelpContent(
                primaryNames.stream().map(OptionName::getValue).collect(Collectors.toList()),
                secondaryNames.stream().map(OptionName::getValue).collect(Collectors.toList()),
                List.of("flag"),
                description,
                getSynopsisSnippet(primaryNames, secondaryNames)
        );
    }

    private String getSynopsisSnippet(List<OptionName> primaryNames, List<OptionName> secondaryNames) {
        List<OptionName> namesToDisplay;
        if (!primaryNames.isEmpty()) {
            namesToDisplay = primaryNames;
        } else {
            namesToDisplay = secondaryNames;
        }
        String synopsisSnippet = namesToDisplay.stream().map(OptionName::getValue).collect(Collectors.joining("|"));
        synopsisSnippet = "[" + synopsisSnippet + "]";
        return synopsisSnippet;
    }

}
