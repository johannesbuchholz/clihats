package io.github.johannesbuchholz.clihats.processor.exceptions;

import io.github.johannesbuchholz.clihats.core.execution.CliException;
import io.github.johannesbuchholz.clihats.core.execution.CliHelpCallException;

import java.io.PrintStream;

public class CliExceptionHandler {

    private final PrintStream infoStream = System.out;
    private final PrintStream errorStream = System.err;

    /**
     * @return the exit code
     */
    public int handle(CliException e) {
        if (e instanceof CliHelpCallException)
            infoStream.println(e.getMessage());
        else
            errorStream.println(e.getMessage());
        return e.getExitCode();
    }

}
