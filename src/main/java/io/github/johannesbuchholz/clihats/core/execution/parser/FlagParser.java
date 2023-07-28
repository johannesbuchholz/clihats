package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.exceptions.parsing.ValueExtractionException;
import io.github.johannesbuchholz.clihats.core.execution.ArgumentParsingResult;
import io.github.johannesbuchholz.clihats.core.execution.InputArgument;
import io.github.johannesbuchholz.clihats.core.execution.ParserHelpContent;
import io.github.johannesbuchholz.clihats.core.execution.ValueMapper;

import java.util.*;

/**
 * <p>Parses arguments like {@code -t}, {@code --option-activated}, {@code --blubb} without an additional value.</p>
 * <p>Returns {@code null} or the specified default value if this argument is not among the given input arguments.
 * Otherwise, returns the stored flag value. The default flag value is the empty string.</p>
 * @param <T> the type this parser returns.
 */
public class FlagParser<T> extends AbstractOptionParser {

    private final Set<OptionName> names;
    private final String flagValue;
    private final String defaultValue;
    private final ValueMapper<T> valueMapper;
    private final String description;

    protected static FlagParser<String> forName(String name, String... names) {
        return new FlagParser<>(asOptionNames(name, names), "", null, s -> s, null);
    }

    private FlagParser(Set<OptionName> names, String flagValue, String defaultValue, ValueMapper<T> valueMapper, String description) {
        this.names = names;
        this.flagValue = flagValue;
        this.defaultValue = defaultValue;
        this.valueMapper = valueMapper;
        this.description = Objects.requireNonNullElse(description, "").trim();
    }

    public FlagParser<T> withFlagValue(String flagValue) {
        return new FlagParser<>(names, Objects.requireNonNull(flagValue), defaultValue, valueMapper, description);
    }

    public FlagParser<T> withDefault(String defaultValue) {
        return new FlagParser<>(names, flagValue, defaultValue, valueMapper, description);
    }

    public <X> FlagParser<X> withMapper(ValueMapper<X> valueMapper) {
        return new FlagParser<>(names, flagValue, defaultValue, Objects.requireNonNull(valueMapper), description);
    }

    public FlagParser<T> withDescription(String description) {
        return new FlagParser<>(names, flagValue, defaultValue, valueMapper, Objects.requireNonNullElse(description, "").trim());
    }

    @Override
    Set<OptionName> getNames() {
        return names;
    }

    @Override
    protected ArgumentParsingResult parse(InputArgument[] inputArgs, int index) throws ValueExtractionException {
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
        return ArgumentParsingResult.of(valueMapper.mapWithThrows(flagValue));
    }

    @Override
    protected ArgumentParsingResult defaultValue() throws ValueExtractionException {
        return ArgumentParsingResult.of(valueMapper.mapWithThrows(defaultValue));
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
        return new ParserHelpContent(primaryNames, secondaryNames, List.of("flag"), description);
    }

}
