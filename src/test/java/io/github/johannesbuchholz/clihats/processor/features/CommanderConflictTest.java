package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.Commander;
import io.github.johannesbuchholz.clihats.core.execution.exception.CommanderCreationException;
import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsers;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CommanderConflictTest {

    @Test
    public void testConflict_shouldDetectCommandsWithSameName() {
        String repeatedCommandName = "samename";
        CommanderCreationException actualException = assertThrows(CommanderCreationException.class, () ->
                Commander.forName("conflictCommander-CommandNames")
                        .withCommands(
                                Command.forName(repeatedCommandName),
                                Command.forName("my-other-command-name"),
                                Command.forName(repeatedCommandName)));
        assertTrue(actualException.getMessage().contains(repeatedCommandName));
    }

    @Test
    public void testConflict_shouldDetectArgumentsWithSameName_NamedAndNamed() {
        String commandName = "commandName";
        String repeatedArgName = "--samename";
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class, () ->
                Commander.forName("conflictCommander-ArgumentNames")
                        .withCommands(
                                Command.forName(commandName)
                                        .withParsers(
                                                ArgumentParsers.valuedOption(repeatedArgName),
                                                ArgumentParsers.operand(0),
                                                ArgumentParsers.flagOption("-f"),
                                                ArgumentParsers.valuedOption(repeatedArgName)
                                        )
                        ));
        assertTrue(actualException.getMessage().contains(repeatedArgName));
    }

    @Test
    public void testConflict_shouldDetectArgumentsWithSameName_FlagAndFlag() {
        String commandName = "commandName";
        String repeatedArgName = "--samename";
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class, () ->
                Commander.forName("conflictCommander-ArgumentNames")
                        .withCommands(
                                Command.forName(commandName)
                                        .withParsers(
                                                ArgumentParsers.flagOption(repeatedArgName),
                                                ArgumentParsers.operand(0),
                                                ArgumentParsers.flagOption("-f"),
                                                ArgumentParsers.flagOption(repeatedArgName)
                                        )
                        ));
        assertTrue(actualException.getMessage().contains(repeatedArgName));
    }

    @Test
    public void testConflict_shouldDetectArgumentsWithSameName_PositionalAndPositional() {
        String commandName = "commandName";
        int repeatedPos = 0;
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class, () ->
                Commander.forName("conflictCommander-ArgumentNames")
                        .withCommands(
                                Command.forName(commandName)
                                        .withParsers(
                                                ArgumentParsers.valuedOption("-n"),
                                                ArgumentParsers.operand(repeatedPos),
                                                ArgumentParsers.flagOption("-f"),
                                                ArgumentParsers.operand(repeatedPos)
                                        )
                        ));

        assertTrue(actualException.getMessage().contains(String.valueOf(repeatedPos)));
    }

    @Test
    public void testConflict_shouldDetectArgumentsWithSameName_NamedAndFlag() {
        String commandName = "commandName";
        String repeatedArgName = "--samename";
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class, () ->
                Commander.forName("conflictCommander-ArgumentNames")
                        .withCommands(
                                Command.forName(commandName)
                                        .withParsers(
                                                ArgumentParsers.valuedOption(repeatedArgName),
                                                ArgumentParsers.operand(0),
                                                ArgumentParsers.flagOption("-f"),
                                                ArgumentParsers.flagOption(repeatedArgName)
                                        )
                        ));
        assertTrue(actualException.getMessage().contains(repeatedArgName));
    }

}
