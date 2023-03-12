package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.AbstractOptionParser;
import io.github.johannesbuchholz.clihats.core.execution.ValueMapper;

import java.util.*;

/**
 * <p>Parses arguments like {@code -t}, {@code -option-activated}, {@code -blubb} without an additional value.</p>
 * <p>Returns {@code null} or the specified default value if this argument is not among the given input arguments.
 * Otherwise, returns the stored flag value. The default flag value is the empty string.</p>
 * @param <T> the type this parser returns.
 */
public class FlagOptionParser<T> extends AbstractOptionParser<T> {

    private static final String FLAG_TEXT = "flag";

    private final String[] names;
    private final String flagValue;
    private final String defaultValue;
    private final ValueMapper<T> valueMapper;
    private final String description;

    protected static FlagOptionParser<String> forName(String name) {
        return new FlagOptionParser<>(new String[]{Objects.requireNonNull(name)}, "", null, s -> s, null);
    }

    private FlagOptionParser(String[] names, String flagValue, String defaultValue, ValueMapper<T> valueMapper, String description) {
        this.flagValue = Objects.requireNonNull(flagValue);
        verifyNames(names);
        this.names = Arrays.stream(names).map(String::trim).toArray(String[]::new);
        this.defaultValue = defaultValue;
        this.valueMapper = Objects.requireNonNull(valueMapper);
        this.description = Objects.requireNonNullElse(description, "").trim();
    }

    public FlagOptionParser<T> withFlagValue(String flagValue) {
        return new FlagOptionParser<>(names, flagValue, defaultValue, valueMapper, description);
    }

    public FlagOptionParser<T> withDefault(String defaultValue) {
        return new FlagOptionParser<>(names, flagValue, defaultValue, valueMapper, description);
    }

    public <X> FlagOptionParser<X> withMapper(ValueMapper<X> valueMapper) {
        return new FlagOptionParser<>(names, flagValue, defaultValue, valueMapper, description);
    }

    public FlagOptionParser<T> withAliases(String... aliases) {
        String[] newNames = Arrays.copyOf(names, names.length + aliases.length);
        System.arraycopy(aliases, 0, newNames, names.length, aliases.length);
        return new FlagOptionParser<>(newNames, flagValue, defaultValue, valueMapper, description);
    }

    public FlagOptionParser<T> withDescription(String description) {
        return new FlagOptionParser<>(names, flagValue, defaultValue, valueMapper, description);
    }

    private void verifyNames(String[] names) throws IllegalArgumentException {
        Set<String> existingNames = new HashSet<>();
        for (String name : names) {
            Objects.requireNonNull(name, "Provided name or alias is null");
            if (existingNames.contains(name)) {
                throw new IllegalArgumentException("Can not create Parser: names or alias " + name + " appears multiple times");
            }
            existingNames.add(name);
        }
    }

    private boolean isMatching(String candidate) {
        for (String alias : names) {
            if (alias.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] getNames() {
        return names;
    }

    @Override
    protected OptionParsingResult parse(List<String> inputArgs) {
        for (String currentArg : inputArgs) {
            if (isMatching(currentArg)) {
                return createMappedResult(flagValue, valueMapper, currentArg);
            }
        }
        return createMappedResult(defaultValue, valueMapper);
    }

    @Override
    protected OptionHelpContent getHelpContent() {
        return new OptionHelpContent(
                getPrimaryName(),
                Arrays.copyOfRange(names, 1, names.length),
                new String[]{FLAG_TEXT},
                description
        );
    }

    @Override
    protected int getParsingOrderWeight() {
        return 0;
    }

}
