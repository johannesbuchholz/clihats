package io.github.johannesbuchholz.clihats.core.execution.parser;

/**
 * Utility class for static factory methods creating option parsers.
 */
public class Parsers {

    private Parsers() {
        // do not instantiate
    }

    public static PositionalParser<String> positional(int position) {
        return PositionalParser.at(position);
    }

    public static FlagParser<String> flag(String name, String... names) {
        return FlagParser.forName(name, names);
    }

    public static ValuedParser<String> valued(String name, String... names) {
        return ValuedParser.forName(name, names);
    }

}
