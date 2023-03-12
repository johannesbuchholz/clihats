package io.github.johannesbuchholz.clihats.core.execution.parser;

/**
 * Utility class for static factory methods creating option parsers.
 */
public class OptionParsers {

    private OptionParsers() {
        // do not instantiate
    }

    public static PositionalOptionParser<String> positional(int position) {
        return PositionalOptionParser.at(position);
    }

    public static FlagOptionParser<String> flag(String name) {
        return FlagOptionParser.forName(name);
    }

    public static ValuedOptionParser<String> valued(String name) {
        return ValuedOptionParser.forName(name);
    }

}
