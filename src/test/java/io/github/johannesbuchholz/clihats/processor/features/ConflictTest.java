package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.core.exceptions.CommandCreationException;
import io.github.johannesbuchholz.clihats.core.exceptions.CommanderCreationException;
import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.Commander;
import io.github.johannesbuchholz.clihats.core.execution.parser.OptionParsers;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ConflictTest {

    @Test
    public void testConflict_shouldDetectCommandsWithSameName() {
        Exception expectedException = null;
        String repeatedCommandName = "samename";
        try {
            Commander.forName("conflictCommander-CommandNames")
                    .withCommands(
                            Command.forName(repeatedCommandName),
                            Command.forName("my other repeatedCommandName"),
                            Command.forName(repeatedCommandName)
                    );
        } catch (CommanderCreationException e) {
            expectedException = e;
        }

        System.out.println("Excpetion:" + expectedException);
        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(repeatedCommandName));
    }

    @Test
    public void testConflict_shouldDetectArgumentsWithSameName_NamedAndNamed() {
        Exception expectedException = null;
        String commandName = "commandName";
        String repeatedArgName = "samename";
        try {
            Commander.forName("conflictCommander-ArgumentNames")
                    .withCommands(
                            Command.forName(commandName)
                                    .withParsers(
                                            OptionParsers.valued(repeatedArgName),
                                            OptionParsers.positional(0),
                                            OptionParsers.flag("-f"),
                                            OptionParsers.valued(repeatedArgName)
                                    )
                    );
        } catch (CommandCreationException e) {
            expectedException = e;
        }

        System.out.println("Received Exception: " + expectedException);
        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(repeatedArgName));
        assertTrue(expectedException.getMessage().contains(commandName));
    }

    @Test
    public void testConflict_shouldDetectArgumentsWithSameName_FlagAndFlag() {
        Exception expectedException = null;
        String commandName = "commandName";
        String repeatedArgName = "samename";
        try {
            Commander.forName("conflictCommander-ArgumentNames")
                    .withCommands(
                            Command.forName(commandName)
                                    .withParsers(
                                            OptionParsers.flag(repeatedArgName),
                                            OptionParsers.positional(0),
                                            OptionParsers.flag("-f"),
                                            OptionParsers.flag(repeatedArgName)
                                    )
                    );
        } catch (CommandCreationException e) {
            expectedException = e;
        }

        System.out.println("Received Exception: " + expectedException);
        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(repeatedArgName));
        assertTrue(expectedException.getMessage().contains(commandName));
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
                                            OptionParsers.valued("-n"),
                                            OptionParsers.positional(repeatedPos),
                                            OptionParsers.flag("-f"),
                                            OptionParsers.positional(repeatedPos)
                                    )
                    );
        } catch (CommandCreationException e) {
            expectedException = e;
        }

        System.out.println("Received Exception: " + expectedException);
        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(String.valueOf(repeatedPos)));
        assertTrue(expectedException.getMessage().contains(commandName));
    }

    @Test
    public void testConflict_shouldDetectArgumentsWithSameName_NamedAndFlag() {
        Exception expectedException = null;
        String commandName = "commandName";
        String repeatedArgName = "samename";
        try {
            Commander.forName("conflictCommander-ArgumentNames")
                    .withCommands(
                            Command.forName(commandName)
                                    .withParsers(
                                            OptionParsers.valued(repeatedArgName),
                                            OptionParsers.positional(0),
                                            OptionParsers.flag("-f"),
                                            OptionParsers.flag(repeatedArgName)
                                    )
                    );
        } catch (CommandCreationException e) {
            expectedException = e;
        }

        System.out.println("Received Exception: " + expectedException);
        assertNotNull(expectedException);
        assertTrue(expectedException.getMessage().contains(repeatedArgName));
        assertTrue(expectedException.getMessage().contains(commandName));
    }

    @Test
    public void testConflict_shouldNotDetectCommandAndArgumentConflictOnLastNamePart_withinSameCommand() {
        Exception expectedException = null;
        String commandName = "commandName second-part one";
        String conflictingArgName = "one";
        try {
            Commander.forName("conflictCommander-ArgumentNames")
                    .withCommands(
                            Command.forName(commandName)
                                    .withParsers(
                                            OptionParsers.valued(conflictingArgName)
                                    )
                    );
        } catch (CommandCreationException e) {
            expectedException = e;
        }

        assertNull(expectedException);
    }

    @Test
    public void testConflict_shouldDetectCommandAndArgumentConflictOnLastNamePart_differentCommands() {
        Exception expectedException = null;
        String ambiguousCommandNameLong = "commandName second-part one";
        String ambiguousCommandNameShort = "commandName second-part";
        String conflictingArgName1 = "one";
        String conflictingArgName2 = "second-part";
        try {
            Commander.forName("conflictCommander-ArgumentNames")
                    .withCommands(
                            Command.forName(ambiguousCommandNameLong)
                                    .withParsers(
                                            OptionParsers.positional(0)
                                    ),
                            Command.forName(ambiguousCommandNameShort)
                                    .withParsers(
                                            OptionParsers.valued("-o")
                                                    .withAliases(conflictingArgName1, "two", "three"),
                                            OptionParsers.flag("-f")
                                                    .withAliases("any", conflictingArgName2)
                                    )
                    );
        } catch (CommanderCreationException e) {
            expectedException = e;
        }
        System.out.println("Received Exception: " + expectedException);

        assertNotNull(expectedException);
        List<String> expectedWords = List.of(ambiguousCommandNameLong, ambiguousCommandNameShort, conflictingArgName1, conflictingArgName2);
        assertTrue(expectedWords.stream().allMatch(expectedException.getMessage()::contains));
    }

}
