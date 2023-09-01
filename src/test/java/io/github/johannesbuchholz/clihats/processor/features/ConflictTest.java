package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.core.exceptions.CommanderCreationException;
import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.Commander;
import io.github.johannesbuchholz.clihats.core.execution.parser.Parsers;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConflictTest {

    @Test
    public void testConflict_shouldDetectCommandsWithSameName() {
        Exception expectedException = null;
        String repeatedCommandName = "samename";
        try {
            Commander.forName("conflictCommander-CommandNames")
                    .withCommands(
                            Command.forName(repeatedCommandName),
                            Command.forName("my-other-command-name"),
                            Command.forName(repeatedCommandName)
                    );
        } catch (CommanderCreationException e) {
            expectedException = e;
        }

        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(repeatedCommandName));
    }

    @Test
    public void testConflict_shouldDetectArgumentsWithSameName_NamedAndNamed() {
        Exception expectedException = null;
        String commandName = "commandName";
        String repeatedArgName = "--samename";
        try {
            Commander.forName("conflictCommander-ArgumentNames")
                    .withCommands(
                            Command.forName(commandName)
                                    .withParsers(
                                            Parsers.valued(repeatedArgName),
                                            Parsers.positional(0),
                                            Parsers.flag("-f"),
                                            Parsers.valued(repeatedArgName)
                                    )
                    );
        } catch (IllegalArgumentException e) {
            expectedException = e;
        }

        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(repeatedArgName));
    }

    @Test
    public void testConflict_shouldDetectArgumentsWithSameName_FlagAndFlag() {
        Exception expectedException = null;
        String commandName = "commandName";
        String repeatedArgName = "--samename";
        try {
            Commander.forName("conflictCommander-ArgumentNames")
                    .withCommands(
                            Command.forName(commandName)
                                    .withParsers(
                                            Parsers.flag(repeatedArgName),
                                            Parsers.positional(0),
                                            Parsers.flag("-f"),
                                            Parsers.flag(repeatedArgName)
                                    )
                    );
        } catch (IllegalArgumentException e) {
            expectedException = e;
        }

        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(repeatedArgName));
    }

    @Test
    public void testConflict_shouldDetectArgumentsWithSameName_PositionalAndPositional() {
        Exception expectedException = null;
        String commandName = "commandName";
        int repeatedPos = 0;
        try {
            Commander.forName("conflictCommander-ArgumentNames")
                    .withCommands(
                            Command.forName(commandName)
                                    .withParsers(
                                            Parsers.valued("-n"),
                                            Parsers.positional(repeatedPos),
                                            Parsers.flag("-f"),
                                            Parsers.positional(repeatedPos)
                                    )
                    );
        } catch (IllegalArgumentException e) {
            expectedException = e;
        }

        System.out.println("Received Exception: " + expectedException);
        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(String.valueOf(repeatedPos)));
    }

    @Test
    public void testConflict_shouldDetectArgumentsWithSameName_NamedAndFlag() {
        Exception expectedException = null;
        String commandName = "commandName";
        String repeatedArgName = "--samename";
        try {
            Commander.forName("conflictCommander-ArgumentNames")
                    .withCommands(
                            Command.forName(commandName)
                                    .withParsers(
                                            Parsers.valued(repeatedArgName),
                                            Parsers.positional(0),
                                            Parsers.flag("-f"),
                                            Parsers.flag(repeatedArgName)
                                    )
                    );
        } catch (IllegalArgumentException e) {
            expectedException = e;
        }

        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(repeatedArgName));
    }

}
