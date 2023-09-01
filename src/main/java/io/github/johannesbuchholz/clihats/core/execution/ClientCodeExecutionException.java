package io.github.johannesbuchholz.clihats.core.execution;

/**
 * Wrapper for an exception caused by invoking {@link Instruction}.
 */
public class ClientCodeExecutionException extends CommandExecutionException {

    public ClientCodeExecutionException(Command failingCommand, Throwable e) {
        super(failingCommand, e);
    }

}
