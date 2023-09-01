package io.github.johannesbuchholz.clihats.core.execution;

/**
 * Thrown when user input requests help.
 * This Exception is intended to shield the command executing object from worrying about how to bring the help message
 * to the user. Instead, the calling code may handle this exception and decide how to use the contained help message.
 */
public class CliHelpCallException extends CliException {

    public CliHelpCallException(String helpMessage) {
        super(helpMessage);
    }

    @Override
    public int getExitCode() {
        return 0;
    }

}
