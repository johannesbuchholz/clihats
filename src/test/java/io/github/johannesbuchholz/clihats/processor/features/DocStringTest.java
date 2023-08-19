package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.core.exceptions.execution.CliException;
import io.github.johannesbuchholz.clihats.core.exceptions.execution.CliHelpCallException;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import io.github.johannesbuchholz.clihats.processor.subjects.Cli1;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DocStringTest {

    private static CliException executeAndGetException(String[] args) {
        CliException cliException = null;
        try {
            CliHats.get(Cli1.class).executeWithThrows(args);
        } catch (CliException e) {
            cliException = e;
        }
        return cliException;
    }

    @Test
    public void testCommanderDescriptionByDocString() {
        // given
        String[] args = {"--help"};

        // when
        CliException actualException = executeAndGetException(args);

        // then
        String expected = "Help for cli-1\n" +
                "Does stuff for testing purposes. Actually it does nothing of value... This should appear in the     \n" +
                "description of the cli.  ";
        assertNotNull(actualException);
        assertEquals(CliHelpCallException.class, actualException.getClass());

        assertStringStartsWith(actualException.getMessage(), expected);
    }

    @Test
    public void testCommandDescriptionByDocString() {
        // given
        String commandName = "command1";
        String[] args = {commandName, "--help"};

        // when
        CliException actualException = executeAndGetException(args);

        // then
        String expected =
                "Help for command1                                                               \n" +
                "This is the first method that is called by {@link Cli1}, when invoked with the  \n" +
                "right arguments. This is another line of text. One will never know.             \n";
        assertNotNull(actualException);
        assertEquals(CliHelpCallException.class, actualException.getClass());

        assertStringStartsWith(actualException.getMessage(), expected);
    }

    @Test
    public void testArgumentDescriptionByDocString() {
        // given
        String commandName = "command2";
        String[] args = {commandName, "--help"};

        // when
        CliException actualException = executeAndGetException(args);

        // then
        String expected =
                        "Help for command2                                                               \n" +
                        "\n" +
                        "Options:                                                                        \n" +
                        "   --opt (flag)     This is a lengthy description for a string argument.";
        assertNotNull(actualException);
        assertEquals(CliHelpCallException.class, actualException.getClass());

        assertStringStartsWith(actualException.getMessage(), expected);
    }

    @Test
    public void runWithJavadocTest() {
        // given
        String commandName = "run-with-javadoc";
        String[] args = {commandName, "--help"};

        // when
        CliException actualException = executeAndGetException(args);
        List<String> expectedDocSequences = List.of(
                "description read from javadoc including this '@' symbol and",
                "this linebreak. Because this is very long...",
                "this will appear in the option description",
                "this should be the last option in the help text."
        );
        List<String> notExpectedDocSequences = List.of(
                "description that is ignored due to explicit \"description\"-parameter"
        );

        // then
        assertNotNull(actualException);
        assertEquals(CliHelpCallException.class, actualException.getClass());

        String actualDoc = actualException.getMessage();
        for (String expected : expectedDocSequences)
            assertStringContains(actualDoc, expected);
        for (String notExpected : notExpectedDocSequences)
            assertStringDoesNotContain(actualDoc, notExpected);
    }

    private void assertStringContains(String actual, String expr) {
        if (!actual.contains(expr))
            throw new AssertionFailedError(String.format("\n---\n%s\n---\n does not match expression \n---\n%s\n---", actual, expr));
    }

    private void assertStringDoesNotContain(String actual, String expr) {
        if (actual.contains(expr))
            throw new AssertionFailedError(String.format("\n---\n%s\n---\n contains expression \n---\n%s\n---", actual, expr));
    }

    private void assertStringStartsWith(String actual, String expr) {
        if (!actual.startsWith(expr))
            throw new AssertionFailedError(String.format("\n---\n%s\n---\n does not start with expression \n---\n%s\n---", actual, expr));
    }

}
