package io.github.johannesbuchholz.clihats.core.execution.exception;

import io.github.johannesbuchholz.clihats.core.execution.Commander;

/**
 * Thrown when creating  {@link Commander} fails due to invalid configuration.
 */
public class CommanderCreationException extends RuntimeException {

    public CommanderCreationException(Commander failingCommander, String message) {
        super(String.format("Could not create Commander %s: %s", failingCommander.getName(), message));
    }

}
