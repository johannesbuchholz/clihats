package io.github.johannesbuchholz.clihats.core.execution.parser.exception;

import io.github.johannesbuchholz.clihats.core.execution.ArgumentParser;
import io.github.johannesbuchholz.clihats.core.execution.exception.ArgumentParsingException;

/**
 * Indicates a parser found its argument but could not map the extracted value.
 */
public class ValueMappingException extends ArgumentParsingException {

    public ValueMappingException(ArgumentParser<?> failingParser, Throwable e) {
        super(String.format("Could not map value of %s: %s", failingParser, e), e);
    }

}
