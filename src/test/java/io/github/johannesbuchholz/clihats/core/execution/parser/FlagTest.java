package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.TestResult;
import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.exception.CommandExecutionException;
import io.github.johannesbuchholz.clihats.core.execution.exception.InvalidInputArgumentException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.UnknownArgumentException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.ValueMappingException;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Objects;

import static org.junit.Assert.*;

public class FlagTest {

    /*

    SUCCESS TESTS

     */

    @Test
    public void shouldExecute_returnInputArg() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(FlagOptionParser.forName(name));
        String[] args = {name};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected("");
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnInputArgUsingAlias() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        String alias = "--abc";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(FlagOptionParser
                        .forName(name, alias)
                );
        String[] args = {alias};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected("");
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnDefaultValueNull_whenMissing() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(FlagOptionParser.forName(name));
        String[] args = {};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected((Object) null);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnCustomDefaultValue_whenMissing() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String defaultValue = "default on missing";
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(FlagOptionParser
                        .forName(name)
                        .withDefault(defaultValue)
                );
        String[] args = {};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(defaultValue);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnEmptyFlagValue_whenNotSpecified() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(FlagOptionParser
                        .forName(name)
                );
        String[] args = {name};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected("");
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnCustomFlagValue() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String flagValue = "a string argument";
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(FlagOptionParser
                        .forName(name)
                        .withFlagValue(flagValue)
                );
        String[] args = {name};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(flagValue);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnMappedValue_Boolean() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(FlagOptionParser
                        .forName(name)
                        .withMapper(Objects::nonNull)
                );

        String[] args = {name};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(Boolean.TRUE);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnMappedValue_CustomMapper() throws CommandExecutionException {
        // given
        ValueMapper<BigDecimal> mapper = input -> input == null ? null : BigDecimal.ONE;

        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(FlagOptionParser
                        .forName(name)
                        .withMapper(mapper)
                );

        String[] args = {name};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(BigDecimal.ONE);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_duplicateNames() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        String[] args = {name};
        // when
        Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(FlagOptionParser
                        .forName(name, name))
                        .execute(args);

        // then
        TestResult expected = TestResult.newExpected("");
        assertEquals(expected, testResult);
    }

    /*

    FAILURE TESTS

     */

    @Test
    public void shouldFail_unknownArgument() {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(FlagOptionParser.forName(name));
        String unknownArg = "-unknown";
        String[] args = {unknownArg};

        // when
        CommandExecutionException actualException = null;
        try {
            c.execute(args);
        } catch (CommandExecutionException e) {
            actualException = e;
        }

        // then
        assertNotNull(actualException);
        assertEquals(InvalidInputArgumentException.class, actualException.getClass());
        assertEquals(UnknownArgumentException.class, actualException.getCause().getClass());
        assertTrue(actualException.getMessage().contains(c.getName()));
        assertTrue(actualException.getMessage().contains(unknownArg));
    }

    @Test
    public void shouldFail_mappingError() {
        // given
        TestResult testResult = TestResult.newEmpty();
        IllegalArgumentException expectedThrow = new IllegalArgumentException("I am the test exception");
        ValueMapper<Object> throwingMapper = str -> {
            throw expectedThrow;
        };
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(FlagOptionParser
                        .forName(name)
                        .withMapper(throwingMapper));
        String[] args = {name};

        // when
        CommandExecutionException actualException = null;
        try {
            c.execute(args);
        } catch (CommandExecutionException e) {
            actualException = e;
        }

        // then
        assertNotNull(actualException);
        assertEquals(InvalidInputArgumentException.class, actualException.getClass());
        assertEquals(ValueMappingException.class, actualException.getCause().getClass());
        assertTrue(actualException.getMessage().contains(c.getName()));
        assertTrue(actualException.getMessage().contains(name));
    }

}
