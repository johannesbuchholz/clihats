package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.core.execution.CliException;
import io.github.johannesbuchholz.clihats.core.execution.exception.CliHelpCallException;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Does stuff for testing purposes. Actually it does nothing of value... This should
 * appear in the description of the cli.
 */
@CommandLineInterface
public class DocStringTest {

    /**
     * Some javadoc explaining this elaborated method.
     *
     * @param stringArg        description read from javadoc including this '@' symbol and including
     *                         this linebreak. Because this is very long...
     * @param descriptionParam description that is ignored due to explicit "description"-parameter
     * @param otherArg         This parameter is not filled by clihats.
     * @param z                this should be the last option in the help text.
     * @throws RuntimeException if something goes wrong.
     * @serialData abcdefg
     * @see CliHats
     * @since 2022
     */
    @Command
    public static void runWithJavadoc(
            @Argument String stringArg,
            @Argument(description = "this will appear in the option description") String descriptionParam,
            String otherArg,
            @Argument String z
    ) {
       throw new IllegalStateException(String.format("This is never reached: run-wth-javadoc: %s", List.of(stringArg, descriptionParam, otherArg, z)));
    }

    /**
     * This is the first method that is called by {@link DocStringTest}, when invoked with the right arguments.
     * This is another line of text. One will never know.
     */
    @Command(name = "command1", cli = DocStringTest.class)
    public static void myMethod1(
            @Argument(type = Argument.Type.OPERAND) String arg1,
            @Argument(name = {"--a2", "--aa2"}) Integer arg2,
            @Argument(name = "--a3", defaultValue = "/my/default/path") Path arg3
    ) {
       throw new IllegalStateException(String.format("This is never reached: command1: %s", List.of(arg1, arg2, arg3)));
    }

    @Command(name = "command2", cli = DocStringTest.class)
    public static void myMethod2(
            @Argument(name = "-r", necessity = Argument.Necessity.REQUIRED) String r,
            @Argument(name = "--opt", flagValue = "Option-On", description = "This is a lengthy description for a string argument.") String arg1
    ) {
       throw new IllegalStateException(String.format("This is never reached: command2: %s", List.of(r, arg1)));
    }

    @Test
    public void testCommanderDescriptionByDocString() {
        // given
        String[] args = {"--help"};

        // when
        // then
        CliException actualException = assertThrows(CliException.class, () -> CliHats.get(DocStringTest.class).executeWithThrows(args));
        String expected = "Help for doc-string-test\n" +
                "Does stuff for testing purposes. Actually it does nothing of value... This should appear in the     \n" +
                "description of the cli.";
        assertEquals(CliHelpCallException.class, actualException.getClass());
        assertStringStartsWith(actualException.getMessage(), expected);
    }

    @Test
    public void testCommandDescriptionByDocString() {
        // given
        String commandName = "command1";
        String[] args = {commandName, "--help"};

        // when
        // then
        CliException actualException = assertThrows(CliException.class, () -> CliHats.get(DocStringTest.class).executeWithThrows(args));
        String expected = "Help for command1                                                               \n" +
                "\n" +
                "Synopsis:\n" +
                "command1 [--a2|--aa2 <value>] [--a3 <value>] [ARG_1]\n" +
                "\n" +
                "This is the first method that is called by {@link DocStringTest}, when invoked  \n" +
                "with the right arguments. This is another line of text. One will never know.    ";
        assertEquals(CliHelpCallException.class, actualException.getClass());

        assertStringStartsWith(actualException.getMessage(), expected);
    }

    @Test
    public void testArgumentDescriptionByDocString() {
        // given
        String commandName = "command2";
        String[] args = {commandName, "--help"};

        // when
        // then
        CliException actualException = assertThrows(CliException.class, () -> CliHats.get(DocStringTest.class).executeWithThrows(args));
        String expected = "Help for command2                                                               \n" +
                "\n" +
                "Synopsis:\n" +
                "command2 -r <value> [--opt]\n" +
                "\n" +
                "Arguments:                                                                      \n" +
                "-r       (required)                                                     \n" +
                "   --opt (flag)     This is a lengthy description for a string argument.";
        assertEquals(CliHelpCallException.class, actualException.getClass());

        assertStringStartsWith(actualException.getMessage(), expected);
    }

    @Test
    public void runWithJavadocTest() {
        // given
        String commandName = "run-with-javadoc";
        String[] args = {commandName, "--help"};

        // when
        // then
        CliException actualException = assertThrows(CliException.class, () -> CliHats.get(DocStringTest.class).executeWithThrows(args));
        List<String> expectedDocSequences = List.of(
                "description read from javadoc including this '@' symbol and",
                "this linebreak. Because this is very long...",
                "this will appear in the option description",
                "this should be the last option in the help text."
        );
        List<String> notExpectedDocSequences = List.of(
                "description that is ignored due to explicit \"description\"-parameter"
        );
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
