package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.execution.parser.OptionHelpContent;
import io.github.johannesbuchholz.clihats.core.execution.parser.OptionParsingResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Extending classes are able to parse a value from a list of options.
 */
public abstract class AbstractOptionParser<T> {

    /**
     * Array of this parsers name and its aliases.
     * <p>
     *     The first entry represents the actual name whereas the remaining names are treated as aliases.
     * </p>
     * The returned array possesses at least one entry. Each entry contains one single word without leading or trailing
     * whitespaces.
     */
    abstract protected String[] getNames();

    /**
     * Parses the list of input arguments and returns a result containing information about whether this parser found
     * its argument or if an exception occurred.
     * @param inputArgs the arguments to be parsed.
     * @return a result object containing information about the outcome.
     */
    abstract protected OptionParsingResult parse(List<String> inputArgs);

    /**
     * Indicates the order in which evaluation round this parser should be evaluated compared to other parsers.
     * <p>Lower values mean earlier evaluation.</p>
     */
    abstract protected int getParsingOrderWeight();

    protected OptionHelpContent getHelpContent() {
        return OptionHelpContent.empty();
    }

    /**
     * Helper method in order to not clutter the actual implementation code with try-catch blocks.
     */
    protected final OptionParsingResult createMappedResult(String value, ValueMapper<T> valueMapper, String... usedArgs) {
        if (value == null)
            return OptionParsingResult.found(null, usedArgs);
        T mappedValue;
        try {
            mappedValue = valueMapper.map(value);
        } catch (Exception e) {
            return OptionParsingResult.error(e, usedArgs);
        }
        return OptionParsingResult.found(mappedValue, usedArgs);
    }

    /**
     * Returns a list of conflict messages, if registering this with another parser at the same command object.
     */
    protected final List<String> conflictsWith(AbstractOptionParser<?> other) {
        return Arrays.stream(getNames())
                .filter(name -> Arrays.asList(other.getNames()).contains(name))
                .map(name -> String.format("Parser %s conflicts with parser %s on name %s", this, other, name))
                .collect(Collectors.toList());
    }

    /**
     * The first entry of {@link #getNames()} or the empty string if no names were defined.
     */
    public final String getPrimaryName() {
        String[] names = getNames();
        return names.length > 0 ? names[0] : "";
    }

    @Override
    public String toString() {
       return String.format("%s{name=%s}", this.getClass().getSimpleName(), Arrays.toString(getNames()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractOptionParser<?> that = (AbstractOptionParser<?>) o;
        return Arrays.equals(getNames(), that.getNames());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getNames());
    }

}
