package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.exceptions.parsing.ValueExtractionException;

import java.util.Objects;
import java.util.Optional;

/**
 * Extending classes are able to parse a value from a list of options.
 */
public abstract class AbstractParser {

    /**
     * @return The name of this parser.
     */
    @Override
    public abstract String toString();

    abstract protected ParserHelpContent getHelpContent();

    /**
     * Parses the argument from the specified index.
     * <p>
     *     Applies side effects on the argument array when using input arguments by replacing them with null
     *     or an updated InputArgument object.
     * </p>
     * @param inputArgs All available input arguments. Has length ov at least one.
     * @param index The index to be parsed. Does not point to a null element.
     * @return The parsed non-null value if this parser is applicable to the specified index and empty otherwise.
     * @throws ValueExtractionException If an unexpected exception occurred during parsing. Callers may discard this parser for future parsing attempts.
     * @apiNote The parser is expected to remove potentially used arguments from the specified array, even when throwing.
     */
    abstract protected ArgumentParsingResult parse(InputArgument[] inputArgs, int index) throws ValueExtractionException;

    /**
     * @return The default value of this parser if any.
     * @throws ValueExtractionException If an unexpected exception occurred during parsing. Callers may discard this parser for future parsing attempts.
     */
    abstract protected ArgumentParsingResult defaultValue() throws ValueExtractionException;

    // TODO: Implement these methods
    /**
     * @param other Another parser.
     * @return A message describing the conflict with other or empty if not conflicting.
     */
    abstract protected Optional<String> getConflictMessage(AbstractParser other);

    /**
     * @return the desired priority to be called with. Lower values indicate earlier parsing.
     */
    abstract protected int getParsingPriority();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractParser that = (AbstractParser) o;
        return this.toString().equals(that.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.toString());
    }

}
