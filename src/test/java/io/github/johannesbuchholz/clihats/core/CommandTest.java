package io.github.johannesbuchholz.clihats.core;

import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.exception.CommandExecutionException;
import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsers;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.ValueMappingException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class CommandTest {

    @Test
    public void creationFailure_illegalName() {
        List<String> illegalNames = List.of("n ame", " ", "", "name ", "one and two", " name");

        List<IllegalArgumentException> exceptions = new ArrayList<>();
        for (String name : illegalNames) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Command.forName(name));
            exceptions.add(e);
        }

        assertEquals(illegalNames.size(), exceptions.size());
    }

    @Test
    public void mappingFailure() {
        String expectedMessage = "I am a very special one";
        CustomException expectedException = new CustomException(expectedMessage);
        Command command = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(ArgumentParsers.operand(0).withMapper(o -> {
                    throw expectedException;
                }));

        String[] args = {"input-arg"};
        CommandExecutionException actual = assertThrows(CommandExecutionException.class, () -> command.execute(args));
        assertTrue(actual.getMessage().contains(expectedMessage));
        assertTrue(actual.getMessage().contains(expectedException.getClass().getSimpleName()));

        Throwable actualCause = actual.getCause();
        assertEquals(ValueMappingException.class, actualCause.getClass());
        assertEquals(expectedException, actualCause.getCause());
    }

    private static class CustomException extends RuntimeException {
        public CustomException(String message) {
            super(message);
        }
    }

}
