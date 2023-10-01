package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.subjects.CliTestInvoker;
import io.github.johannesbuchholz.clihats.processor.subjects.CliTestingCustomException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CustomExceptionTest {

    @Test
    public void expectCustomExceptionMessage() {
        // given
        String value = "value";
        String[] args = {"throwing-command", "-a", value};
        // when
        CliTestInvoker.testGeneratedCli(CliTestingCustomException.class, args);
        // then
        String expectedErrorMessage = CliTestingCustomException.CUSTOM_EXCEPTION_MESSAGE + value;
        Throwable actualException = GlobalTestResult.waitForResult().getException();
        Throwable actualRootCause = getRootCause(actualException);

        assertTrue(actualException.getMessage().contains(CliTestingCustomException.COMMANDER_NAME));
        assertEquals(expectedErrorMessage, actualRootCause.getMessage());
    }

    @Test
    public void expectCustomException() {
        // given
        String value = "value";
        String[] args = {"throwing-command-with-cause", "-a", value};
        // when
        CliTestInvoker.testGeneratedCli(CliTestingCustomException.class, args);
        // then
        String expectedErrorMessage = CliTestingCustomException.CUSTOM_EXCEPTION_MESSAGE + value;
        UnsupportedOperationException expectedRootCause = new UnsupportedOperationException(expectedErrorMessage);
        Throwable actualException = GlobalTestResult.waitForResult().getException();
        Throwable actualRootCause = getRootCause(actualException);

        assertTrue(actualException.getMessage().contains(CliTestingCustomException.COMMANDER_NAME));
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
