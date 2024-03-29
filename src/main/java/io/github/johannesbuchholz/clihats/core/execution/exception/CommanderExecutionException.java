package io.github.johannesbuchholz.clihats.core.execution.exception;

import io.github.johannesbuchholz.clihats.core.execution.CliException;
import io.github.johannesbuchholz.clihats.core.execution.Commander;

public class CommanderExecutionException extends CliException {

    private static String generateMessagePrefix(String cliName) {
        return String.format("Error running " + cliName + ": ");
    }

    protected CommanderExecutionException(Commander failingCommander, String message) {
        super(generateMessagePrefix(failingCommander.getName()) + message);
    }

    public CommanderExecutionException(Commander failingCommander, CommandExecutionException e) {
        super(generateMessagePrefix(failingCommander.getName()) + e.getMessage(), e);
    }

}
