package io.github.johannesbuchholz.clihats.core.exceptions.execution;

import io.github.johannesbuchholz.clihats.core.execution.Command;

public abstract class CommandExecutionException extends CliException {

    private static String generateMessagePrefix(String commandName) {
        return "Exception during invocation of command " + commandName + ": ";
    }

    protected CommandExecutionException(Command failingCommand, String message) {
        super(generateMessagePrefix(failingCommand.getDisplayName()) + message);
    }

    protected CommandExecutionException(Command failingCommand, Throwable cause) {
        super(generateMessagePrefix(failingCommand.getDisplayName()) + cause.getMessage(), cause);
    }

}
