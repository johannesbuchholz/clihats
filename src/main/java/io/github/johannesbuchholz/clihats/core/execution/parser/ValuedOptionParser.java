package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.exceptions.MissingValueException;
import io.github.johannesbuchholz.clihats.core.execution.AbstractOptionParser;
import io.github.johannesbuchholz.clihats.core.execution.ValueMapper;

import java.util.*;
import java.util.function.Supplier;

/**
 * Parses named arguments with user input values like
 * <p>-a=25, -my-argument 42, -stp:some-string</p>
 *
 * @param <T> the type this parser returns.
 */
public class ValuedOptionParser<T> extends AbstractOptionParser<T> {

    private final String[] names;
    private final String delimiter;
    private final boolean required;
    private final ValueMapper<T> valueMapper;
    private final String description;
    private final Supplier<String> defaultSupplier;

    protected static ValuedOptionParser<String> forName(String name) {
        return new ValuedOptionParser<>(new String[]{name}, "", () -> null, false, str -> str, "");
    }

    private ValuedOptionParser(String[] names, String delimiter, Supplier<String> defaultSupplier, boolean required, ValueMapper<T> valueMapper, String description) {
        verifyNames(names);
        this.names = Arrays.stream(names).map(String::trim).toArray(String[]::new);
        this.delimiter = Objects.requireNonNullElse(delimiter, "").trim();
        this.required = required;
        this.valueMapper = Objects.requireNonNull(valueMapper);
        this.description = Objects.requireNonNullElse(description, "").trim();
        this.defaultSupplier = defaultSupplier;
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

    // builder like methods

    public <X> ValuedOptionParser<X> withMapper(ValueMapper<X> mapper) {
        return new ValuedOptionParser<>(names, delimiter, defaultSupplier, required, mapper, description);
    }

    public ValuedOptionParser<T> withDelimiter(String delimiter) {
        return new ValuedOptionParser<>(names, delimiter, defaultSupplier, required, valueMapper, description);
    }

    public ValuedOptionParser<T> withDefault(String defaultValue) {
        return new ValuedOptionParser<>(names, delimiter, () -> defaultValue, required, valueMapper, description);
    }

    public ValuedOptionParser<T> withDefault(Supplier<String> inputSupplier) {
        return new ValuedOptionParser<>(names, delimiter, inputSupplier, required, valueMapper, description);
    }

    public ValuedOptionParser<T> isRequired(boolean required) {
        return new ValuedOptionParser<>(names, delimiter, defaultSupplier, required, valueMapper, description);
    }

    public ValuedOptionParser<T> withAliases(String... aliases) {
        String[] newNames = Arrays.copyOf(names, names.length + aliases.length);
        System.arraycopy(aliases, 0, newNames, names.length, aliases.length);
        return new ValuedOptionParser<>(newNames, delimiter, defaultSupplier, required, valueMapper, description);
    }

    public ValuedOptionParser<T> withDescription(String description) {
        return new ValuedOptionParser<>(names, delimiter, defaultSupplier, required, valueMapper, description);
    }

    // functional

    @Override
    public String[] getNames() {
        return names;
    }

    @Override
    protected OptionParsingResult parse(List<String> inputArgs) {
        for (int i = 0; i < inputArgs.size(); i++) {
            String currentArg = inputArgs.get(i);
            NameMatcher nameMatcher = new NameMatcher(currentArg);
            if (nameMatcher.isMatching()) {
                if (delimiter.isEmpty()) {
                    // take value from next arg
                    if (i + 1 < inputArgs.size()) {
                        String stringValue = inputArgs.get(i + 1);
                        return createMappedResult(stringValue, valueMapper, currentArg, stringValue);
                    } else {
                        return OptionParsingResult.error(new MissingValueException(currentArg), currentArg);
                    }
                } else {
                    // take value from this arg after offset
                    int offset = nameMatcher.getOffset();
                    if (offset < currentArg.length())
                        return createMappedResult(currentArg.substring(offset), valueMapper, currentArg);
                    else
                        return OptionParsingResult.error(new MissingValueException(currentArg), currentArg);
                }
            }
        }
        // here if argument was not found
        if (required)
            return OptionParsingResult.notFound();
        else
            return createMappedResult(defaultSupplier.get(), valueMapper);
    }

    @Override
    protected OptionHelpContent getHelpContent() {
        return new OptionHelpContent(
                getPrimaryName(),
                Arrays.copyOfRange(names, 1, names.length),
                new String[]{
                        required ? OptionHelpContent.REQUIRED_TEXT : null,
                        delimiter.isEmpty() ? null : "delim: " + delimiter},
                description
        );
    }

    @Override
    protected int getParsingOrderWeight() {
        return 0;
    }

    /**
     * Checks if any alias of this parser matches a given input argument field.
     */
    private class NameMatcher {

        private final int offset;

        private NameMatcher(String inputArgument) {
            int searchResult;
            if (delimiter.isEmpty())
                searchResult = searchWithoutDelimiter(inputArgument);
            else
                searchResult = searchWithDelimiter(inputArgument);
            offset = searchResult;
        }

        private int searchWithoutDelimiter(String inputArgument) {
            for (String alias : names) {
                if (alias.equals(inputArgument)) {
                    return 0;
                }
            }
            return -1;
        }

        private int searchWithDelimiter(String inputArgument) {
            for (String alias : names) {
                if (alias.equals(inputArgument) || inputArgument.startsWith(alias + delimiter))
                    return (alias + delimiter).length();
            }
            return -1;
        }

        /**
         * Describes the offset the argument value can be parsed from.
         * <li>-1 indicates that no alias matches the input argument this was created with</li>
         * <li>>=0 indicates that one alias matches the input argument this was created with</li>
         */
        private boolean isMatching() {
            return offset >= 0;
        }

        private int getOffset() {
            return offset;
        }

    }

}
