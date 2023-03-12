package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.annotations.Option;

import static io.github.johannesbuchholz.clihats.processor.subjects.CliTestingCustomException.COMMANDER_NAME;

@CommandLineInterface(name = COMMANDER_NAME)
public class CliTestingCustomException {

    public static final String COMMANDER_NAME = "testing-custom-exceptions-commander";
    public static final String CUSTOM_EXCEPTION_MESSAGE = "My custom exception message: ";

    @Command
    public static void throwingCommand(@Option String a) throws Exception {
        throw new Exception(CUSTOM_EXCEPTION_MESSAGE + a);
    }

    @Command
    public static void throwingCommandWithCause(@Option String a) throws Exception {
        UnsupportedOperationException cause = new UnsupportedOperationException(CUSTOM_EXCEPTION_MESSAGE + a);
        throw new Exception(a, cause);
    }

}
