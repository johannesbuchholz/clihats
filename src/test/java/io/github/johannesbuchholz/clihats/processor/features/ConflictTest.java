package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.Commander;
import io.github.johannesbuchholz.clihats.core.execution.CommanderCreationException;
import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsers;
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
                                            ArgumentParsers.valuedOption(repeatedArgName),
                                            ArgumentParsers.operand(0),
                                            ArgumentParsers.flagOption("-f"),
                                            ArgumentParsers.valuedOption(repeatedArgName)
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
                                            ArgumentParsers.flagOption(repeatedArgName),
                                            ArgumentParsers.operand(0),
                                            ArgumentParsers.flagOption("-f"),
                                            ArgumentParsers.flagOption(repeatedArgName)
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
                                            ArgumentParsers.valuedOption("-n"),
                                            ArgumentParsers.operand(repeatedPos),
                                            ArgumentParsers.flagOption("-f"),
                                            ArgumentParsers.operand(repeatedPos)
                                    )
                    );
        } catch (IllegalArgumentException e) {
            expectedException = e;
        }

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
                                            ArgumentParsers.valuedOption(repeatedArgName),
                                            ArgumentParsers.operand(0),
                                            ArgumentParsers.flagOption("-f"),
                                            ArgumentParsers.flagOption(repeatedArgName)
                                    )
                    );
        } catch (IllegalArgumentException e) {
            expectedException = e;
        }

        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(repeatedArgName));
    }

}
