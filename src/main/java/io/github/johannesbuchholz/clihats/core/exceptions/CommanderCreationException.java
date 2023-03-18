package io.github.johannesbuchholz.clihats.core.exceptions;

import io.github.johannesbuchholz.clihats.core.execution.Commander;

/**
 * Thrown when creating  {@link Commander} fails due to invalid configuration.
 */
public class CommanderCreationException extends RuntimeException {

    public CommanderCreationException(String messageTemplate, Object... args) {
        super(String.format(messageTemplate, args));
    }

}