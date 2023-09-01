package io.github.johannesbuchholz.clihats.core.execution;

public class UnknownCommandException extends CommanderExecutionException {

    public UnknownCommandException(Commander failingCommander, String unknownCommandName) {
        super(failingCommander, "Could not find command " + unknownCommandName);
    }

}
