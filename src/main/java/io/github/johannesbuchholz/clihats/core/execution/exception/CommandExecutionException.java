package io.github.johannesbuchholz.clihats.core.execution.exception;

import io.github.johannesbuchholz.clihats.core.execution.CliException;
import io.github.johannesbuchholz.clihats.core.execution.Command;

public abstract class CommandExecutionException extends CliException {

    private static String generateMessagePrefix(Command command) {
        return "Exception during invocation of command " + command.getName() + ": ";
    }

    protected CommandExecutionException(Command failingCommand, Throwable cause) {
        super(generateMessagePrefix(failingCommand) + cause.getMessage(), cause);
    }

    public CommandExecutionException(Command failingCommand, String message, Throwable cause) {
        super(generateMessagePrefix(failingCommand) + message, cause);
    }

}
