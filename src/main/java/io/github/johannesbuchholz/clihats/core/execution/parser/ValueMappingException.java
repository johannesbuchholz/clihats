package io.github.johannesbuchholz.clihats.core.execution.parser;

/**
 * Indicates a parser found its argument but could not map the extracted value.
 */
public class ValueMappingException extends ArgumentParsingException {

    ValueMappingException(AbstractParser<?> failingParser, Throwable e) {
        super(String.format("Could not map value of %s: %s", failingParser, e.getCause()), e);
    }

}
