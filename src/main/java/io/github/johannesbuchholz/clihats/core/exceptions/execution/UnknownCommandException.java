package io.github.johannesbuchholz.clihats.core.exceptions.execution;

import io.github.johannesbuchholz.clihats.core.execution.Commander;

public class UnknownCommandException extends CommanderExecutionException {

    public UnknownCommandException(Commander failingCommander, String unknownCommandName) {
        super(failingCommander, "Could not find command " + unknownCommandName);
    }

}
