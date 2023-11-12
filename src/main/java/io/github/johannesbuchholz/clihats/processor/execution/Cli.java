package io.github.johannesbuchholz.clihats.processor.execution;

import io.github.johannesbuchholz.clihats.core.execution.CliException;
import io.github.johannesbuchholz.clihats.core.execution.Commander;
import io.github.johannesbuchholz.clihats.processor.exceptions.CliExceptionHandler;

public class Cli {

    private final CliExceptionHandler cliExceptionHandler = new CliExceptionHandler();
    private final Commander commander;

    Cli(Commander commander) {
        this.commander = commander;
    }

    /**
     * Passes the specified arguments to this command-line interface and executes the matching command.
     * <p>{@link CliException} thrown during execution are handled within this method.
     * In such a case, {@link System#exit(int)} will be called with exit code according to the received exception.</p>
     * @param args the arguments to pass to this command-line interface.
     */
    public void execute(String[] args) {
        try {
            commander.execute(args);
        } catch (CliException e) {
            int exitCode = cliExceptionHandler.handle(e);
            System.exit(exitCode);
        }
    }

    /**
     * Passes the specified arguments to this command-line interface and executes the matching command.
     * <p>{@link CliException} thrown during execution are passed to the caller. Use this method if custom
     * exception handling is desired.</p>
     * @param args the arguments to pass to this command-line interface.
     * @throws CliException if argument parsing or execution fails or the arguments represent a help call.
     */
    public void executeWithThrows(String[] args) throws CliException {
        commander.execute(args);
    }

}
