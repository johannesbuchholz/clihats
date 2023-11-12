package io.github.johannesbuchholz.clihats.processor.subjects;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;

import java.nio.file.Path;

/**
 * Does stuff for testing purposes. Actually it does nothing of value... This should
 * appear in the description of the cli.
 */
@CommandLineInterface
public class Cli1 {

    /**
     * This is the first method that is called by {@link Cli1}, when invoked with the right arguments.
     * This is another line of text. One will never know.
     */
    @Command(name = "command1", cli = Cli1.class)
    public static void myMethod1(
            @Argument(type = Argument.Type.OPERAND) String arg1,
            @Argument(name = {"--a2", "--aa2"}) Integer arg2,
            @Argument(name = "--a3", defaultValue = "/my/default/path") Path arg3
    ) {
        GlobalTestResult.setSuccess("command1", arg1, arg2, arg3);
    }

    @Command(name = "command2", cli = Cli1.class)
    public static void myMethod2(
            @Argument(name = "-r", necessity = Argument.Necessity.REQUIRED) String r,
            @Argument(name = "--opt", flagValue = "Option-On", description = "This is a lengthy description for a string argument.") String arg1
    ) {
        GlobalTestResult.setSuccess("command2", r, arg1);
    }

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
        GlobalTestResult.setSuccess("run-wth-javadoc", stringArg, descriptionParam, otherArg, z);
    }

}
