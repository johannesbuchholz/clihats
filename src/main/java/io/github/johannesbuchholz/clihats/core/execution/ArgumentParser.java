package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.MissingArgumentException;

/**
 * Extending classes are able to parse a value from a list of options.
 * @param <T> The type this parser returns.
 */
public interface ArgumentParser<T> {

    /**
     * @return The unique displayable name of this parser.
     * @apiNote Parsers possessing the same id value should be considered equal.
     */
    ParserId getId();

    ParserHelpContent getHelpContent();

    /**
     * Parses the argument from the specified index.
     * <p>
     *     Applies side effects on the argument array when using input arguments by replacing them with null
     *     or an updated InputArgument object.
     * </p>
     * @param inputArgs All available input arguments. Has length ov at least one.
     * @param index The index to be parsed. Does not point to a null element.
     * @return The parsed non-null value if this parser is applicable to the specified index and empty otherwise.
     * @throws MissingArgumentException If an unexpected exception occurred during parsing. Callers may discard this parser for future parsing attempts.
     * @apiNote The parser is expected to remove potentially used arguments from the specified array, even when throwing.
     */
    ArgumentParsingResult<T> parse(InputArgument[] inputArgs, int index) throws ArgumentParsingException;

    /**
     * @return The default value of this parser if any.
     * @throws MissingArgumentException If an unexpected exception occurred during parsing. Callers may discard this parser for future parsing attempts.
     */
    ArgumentParsingResult<T> defaultValue() throws ArgumentParsingException;

}
