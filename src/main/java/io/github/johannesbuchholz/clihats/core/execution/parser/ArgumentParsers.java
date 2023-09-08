package io.github.johannesbuchholz.clihats.core.execution.parser;

/**
 * Utility class for static factory methods creating argument parsers.
 */
public class ArgumentParsers {

    private ArgumentParsers() {
        // do not instantiate
    }

    public static OperandParser<String> operand(int position) {
        return OperandParser.at(position);
    }

    public static FlagParser<String> flagOption(String name, String... names) {
        return FlagParser.forName(name, names);
    }

    public static ValuedParser<String> valuedOption(String name, String... names) {
        return ValuedParser.forName(name, names);
    }

}
