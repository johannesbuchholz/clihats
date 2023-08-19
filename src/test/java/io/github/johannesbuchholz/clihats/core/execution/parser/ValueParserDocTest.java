package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.execution.Command;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ValueParserDocTest {

    @Test
    public void namedParserDoc_noCommandDescription_noArgumentDescription_expect_n1() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(ValuedParser.forName("-a"));

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Options:\n" +
                "-a <No description>";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void namedParserDoc_withCommandDescription_noArgumentDescription_expect_n2() {
        // given
        Command c = Command.forName("run")
                .withDescription("This is a lengthy Description that should be wrapped into a new line. Hopefully this works out fine: A new line for this command. Yeah!")
                .withInstruction(args -> {})
                .withParsers(ValuedParser.forName("-a"));

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "This is a lengthy Description that should be wrapped into a new line. Hopefully\n" +
                "this works out fine: A new line for this command. Yeah!\n" +
                "\n" +
                "Options:\n" +
                "-a <No description>";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void namedParserDoc_noCommandDescription_argumentDescription_expect_n3() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(ValuedParser
                        .forName("-a")
                        .withDescription("Some meaningful argument. Defaults to 'null' if missing.")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Options:\n" +
                "-a Some meaningful argument. Defaults to 'null' if missing.";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void namedParserDoc_noCommandDescription_argumentDescription_required_expect_n4() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(ValuedParser
                        .forName("-a")
                        .withRequired(true)
                        .withDescription("Some meaningful argument. Defaults to 'null' if missing.")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Options:\n" +
                "-a (required) Some meaningful argument. Defaults to 'null' if missing.";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void namedParserDoc_noCommandDescription_argumentDescription_required_withCustomDefault_expect_n4() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(ValuedParser
                        .forName("-a")
                        .withRequired(true)
                        .withDefault("some default")
                        .withDescription("Some meaningful argument. Defaults to 'null' if missing.")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Options:\n" +
                "-a (required) Some meaningful argument. Defaults to 'null' if missing.";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void namedParserDoc_noCommandDescription_argumentDescription_aliases_expect_n6() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(ValuedParser.forName("-a", "--abc", "--abcdefg", "--another-alias")
                        .withDescription("Some meaningful argument. Defaults to 'null' if missing.")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "\n" +
                "Options:\n" +
                "-a --abc           Some meaningful argument. Defaults to 'null' if missing.\n" +
                "   --abcdefg\n" +
                "   --another-alias";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    @Test
    public void namedParserDoc_noCommandDescription_multipleArgumentDescriptions_orderAlphabetically_n7() {
        // given
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withDescription("This is a command that runs into nothing, the great void and across all emptiness of space and time as long as this documentation string is taking to consume all that is left.")
                .withParsers(
                        ValuedParser.forName("-p", "--poop", "--why-so-much", "--POOP")
                                .withDescription("Poopdipoop. Another Description. Not too long."),
                        ValuedParser.forName("-a", "--abc")
                                .withRequired(true)
                                .withDescription("Some meaningful argument. Defaults to 'null' if missing.")
                );

        // when
        String actualDoc = c.getDoc();

        // then
        String expectedDoc = "Help for run\n" +
                "This is a command that runs into nothing, the great void and across all\n" +
                "emptiness of space and time as long as this documentation string is taking to\n" +
                "consume all that is left.\n" +
                "\n" +
                "Options:\n" +
                "-a --abc         (required) Some meaningful argument. Defaults to 'null' if missing.\n" +
                "-p --POOP                   Poopdipoop. Another Description. Not too long.\n" +
                "   --poop\n" +
                "   --why-so-much";
        assertEquals(stripTrailingLinewise(expectedDoc), stripTrailingLinewise(actualDoc));
    }

    private String stripTrailingLinewise(String original) {
        return Arrays.stream(original.split("\n"))
                .map(String::stripTrailing)
                .collect(Collectors.joining("\n"));
    }

}
