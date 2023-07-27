package io.github.johannesbuchholz.clihats.core.exceptions;

import io.github.johannesbuchholz.clihats.core.execution.Command;

/**
 * Thrown when creating {@link Command} fails due to invalid configuration.
 */
public class CommandCreationException extends RuntimeException {

    public CommandCreationException(String messageTemplate, Object... args) {
        super(String.format(messageTemplate, args));
    }

}
