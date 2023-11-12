package io.github.johannesbuchholz.clihats.core.execution.exception;

import io.github.johannesbuchholz.clihats.core.execution.Commander;

/**
 * Thrown when creating  {@link Commander} fails due to invalid configuration.
 */
public class CommanderCreationException extends RuntimeException {

    public CommanderCreationException(String message) {
        super(message);
    }

}
