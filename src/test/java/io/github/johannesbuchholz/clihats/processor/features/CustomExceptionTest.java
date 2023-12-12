package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.core.execution.CliException;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import org.junit.Test;

import static org.junit.Assert.*;

@CommandLineInterface
public class CustomExceptionTest {

    public static final String COMMANDER_NAME = "custom-exception-test";
    public static final String CUSTOM_EXCEPTION_MESSAGE = "My custom exception message: ";

    @Command
    public static void throwingCommand(@Argument String a) throws Exception {
        throw new Exception(CUSTOM_EXCEPTION_MESSAGE + a);
    }

    @Command
    public static void throwingCommandWithCause(@Argument String a) throws Exception {
        UnsupportedOperationException cause = new UnsupportedOperationException(CUSTOM_EXCEPTION_MESSAGE + a);
        throw new Exception(a, cause);
    }

    @Test
    public void expectCustomExceptionMessage() {
        // given
        String value = "value";
        String[] args = {"throwing-command", "-a", value};
        // when
        // then
        String expectedErrorMessage = CUSTOM_EXCEPTION_MESSAGE + value;
        CliException actualException = assertThrows(CliException.class, () -> CliHats.get(CustomExceptionTest.class).executeWithThrows(args));
        Throwable actualRootCause = getRootCause(actualException);

        assertTrue(actualException.getMessage().contains(COMMANDER_NAME));
        assertEquals(expectedErrorMessage, actualRootCause.getMessage());
    }

    @Test
    public void expectCustomException() {
        // given
        String value = "value";
        String[] args = {"throwing-command-with-cause", "-a", value};
        // when
        // then
        String expectedErrorMessage = CUSTOM_EXCEPTION_MESSAGE + value;
        UnsupportedOperationException expectedRootCause = new UnsupportedOperationException(expectedErrorMessage);
        CliException actualException = assertThrows(CliException.class, () -> CliHats.get(CustomExceptionTest.class).executeWithThrows(args));
        Throwable actualRootCause = getRootCause(actualException);

        assertTrue(actualException.getMessage().contains(CustomExceptionTest.COMMANDER_NAME));
        assertEquals(expectedRootCause.getClass(), actualRootCause.getClass());
        assertEquals(expectedRootCause.getMessage(), actualRootCause.getMessage());
    }

    private Throwable getRootCause(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null)
            root = root.getCause();
        return root;
    }

}
