package io.github.johannesbuchholz.clihats.core.execution.exception;

import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.Instruction;

/**
 * Wrapper for an exception caused by invoking {@link Instruction}.
 */
public class ClientCodeExecutionException extends CommandExecutionException {

    public ClientCodeExecutionException(Command failingCommand, Throwable e) {
        super(failingCommand, e);
    }

}
