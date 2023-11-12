package io.github.johannesbuchholz.clihats.core.execution;

import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsers;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class CommandDocTest {

    @Test
    public void valuedParserDoc_noCommandDescription_noArgumentDescription() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(ArgumentParsers.valuedOption("-a"));

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Synopsis:\n" +
                "run [-a <value>]\n" +
                "\n" +
                "Arguments:\n" +
                "-a";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void valuedParserDoc_withCommandDescription_noArgumentDescription() {
        // given
        Command c = Command.forName("run")
                .withDescription("This is a lengthy Description that should be wrapped into a new line. Hopefully this works out fine: A new line for this command. Yeah!")
                .withInstruction(args -> {})
                .withParsers(ArgumentParsers.valuedOption("-a"));

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Synopsis:\n" +
                "run [-a <value>]\n" +
                "\n" +
                "This is a lengthy Description that should be wrapped into a new line. Hopefully\n" +
                "this works out fine: A new line for this command. Yeah!\n" +
                "\n" +
                "Arguments:\n" +
                "-a";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void valuedParserDoc_noCommandDescription_argumentDescription() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(ArgumentParsers.valuedOption("-a")
                        .withDescription("Some meaningful argument. Defaults to 'null' if missing.")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Synopsis:\n" +
                "run [-a <value>]\n" +
                "\n" +
                "Arguments:\n" +
                "-a Some meaningful argument. Defaults to 'null' if missing.";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void valuedParserDoc_noCommandDescription_argumentDescription_required() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(ArgumentParsers.valuedOption("-a")
                        .withRequired(true)
                        .withDescription("Some meaningful argument. Defaults to 'null' if missing.")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Synopsis:\n" +
                "run -a <value>\n" +
                "\n" +
                "Arguments:\n" +
                "-a (required) Some meaningful argument. Defaults to 'null' if missing.";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void valuedParserDoc_noCommandDescription_argumentDescription_required_withCustomDefault() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(ArgumentParsers.valuedOption("-a")
                        .withRequired(true)
                        .withDefault("some default")
                        .withDescription("Some meaningful argument. Defaults to 'null' if missing.")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Synopsis:\n" +
                "run -a <value>\n" +
                "\n" +
                "Arguments:\n" +
                "-a (required) Some meaningful argument. Defaults to 'null' if missing.";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void valuedParserDoc_noCommandDescription_argumentDescription_aliases() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(ArgumentParsers.valuedOption("-a", "--abc", "--abcdefg", "--another-alias")
                        .withDescription("Some meaningful argument. Defaults to 'null' if missing.")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Synopsis:\n" +
                "run [-a <value>]\n" +
                "\n" +
                "Arguments:\n" +
                "-a --abc           Some meaningful argument. Defaults to 'null' if missing.\n" +
                "   --abcdefg\n" +
                "   --another-alias";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void valuedParserDoc_noCommandDescription_multipleArgumentDescriptions_expectOrderAlphabetically() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withDescription("This is a command that runs into nothing, the great void and across all emptiness of space and time as long as this documentation string is taking to consume all that is left.")
                .withParsers(
                        ArgumentParsers.valuedOption("-p", "--poop", "--why-so-much", "--POOP")
                                .withDescription("Poopdipoop. Another Description. Not too long."),
                        ArgumentParsers.valuedOption("-a", "--abc")
                                .withRequired(true)
                                .withDescription("Some meaningful argument. Defaults to 'null' if missing.")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Synopsis:\n" +
                "run -a <value> [-p <value>]\n" +
                "\n" +
                "This is a command that runs into nothing, the great void and across all\n" +
                "emptiness of space and time as long as this documentation string is taking to\n" +
                "consume all that is left.\n" +
                "\n" +
                "Arguments:\n" +
                "-a --abc         (required) Some meaningful argument. Defaults to 'null' if missing.\n" +
                "-p --POOP                   Poopdipoop. Another Description. Not too long.\n" +
                "   --poop\n" +
                "   --why-so-much";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void optionAndOperandParserDoc_operandWithDisplayName() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withDescription("Some text to have anything at all.")
                .withParsers(
                        ArgumentParsers.operand(1)
                                .withDescription("The second operand. This one also has a beautiful name.")
                                .withDisplayName("Karl"),
                        ArgumentParsers.flagOption("-f", "--flag-in-the-wind")
                                .withDescription("Changes as the wind blows."),
                        ArgumentParsers.operand(0)
                                .withRequired(true)
                                .withDescription("First operand"),
                        ArgumentParsers.valuedOption("-p", "--poop")
                                .withDescription("Hope this does not backfire...")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Synopsis:\n" +
                "run [-f] [-p <value>] OPERAND0 [Karl]\n" +
                "\n" +
                "Some text to have anything at all.\n" +
                "\n" +
                "Arguments:\n" +
                "-f       --flag-in-the-wind (flag)     Changes as the wind blows.\n" +
                "-p       --poop                        Hope this does not backfire...\n" +
                "OPERAND0                    (required) First operand\n" +
                "Karl                                   The second operand. This one also has a beautiful name.";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void valuedParserDoc_multiplePrimaryNames() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withDescription("Some description. Not too long but it is there. Here you go!")
                .withParsers(
                        ArgumentParsers.valuedOption("-p", "-q", "-r", "--poop", "--uff")
                                .withDescription("Another Description. Very short."),
                        ArgumentParsers.flagOption("--flag", "-f", "-l", "-A", "-g")
                                .withDescription("So many names for this flag."),
                        ArgumentParsers.valuedOption("--apologies", "--for", "--length")
                                .withRequired(true)
                                .withDescription("Use one of the long names to target this.")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Synopsis:\n" +
                "run [-Afgl] [-pqr <value>] --apologies|--for|--length <value>\n" +
                "\n" +
                "Some description. Not too long but it is there. Here you go!\n" +
                "\n" +
                "Arguments:\n" +
                "-A --flag      (flag)     So many names for this flag.\n" +
                "-f\n" +
                "-g\n" +
                "-l\n" +
                "-p --poop                 Another Description. Very short.\n" +
                "-q --uff\n" +
                "-r\n" +
                "   --apologies (required) Use one of the long names to target this.\n" +
                "   --for\n" +
                "   --length";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    private String stripTrailingLinewise(String original) {
        return Arrays.stream(original.split("\n"))
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"));
    }

}
