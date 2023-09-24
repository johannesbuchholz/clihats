package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.TestResult;
import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.exception.CommandExecutionException;
import io.github.johannesbuchholz.clihats.core.execution.exception.InvalidInputArgumentException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.MissingArgumentException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.MissingValueException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.UnknownArgumentException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.ValueMappingException;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class NameTest {

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
                .withParsers(ValuedOptionParser.forName(name));
        String stringArg = "a string argument";
        String[] args = {name, stringArg};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArg);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_usingSecondPrimaryName() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name1 = "-a";
        String name2 = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ValuedOptionParser.forName(name1, name2));
        String stringArg = "a string argument";
        String[] args = {name2, stringArg};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArg);
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
                .withParsers(ValuedOptionParser
                        .forName(name, alias)
                );
        String stringArg = "a string argument";
        String[] args = {alias, stringArg};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArg);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnDefaultValueNull_whenMissing() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ValuedOptionParser.forName(name));
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
        String defaultValue = "a string argument";
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ValuedOptionParser
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
    public void shouldExecute_returnCustomDefaultSupplierValue_whenMissing() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String defaultValue = "a string argument";
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ValuedOptionParser
                        .forName(name)
                        .withDefault(() -> defaultValue)
                );
        String[] args = {};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(defaultValue);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnMappedValue_BigDecimal() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ValuedOptionParser
                        .forName(name)
                        .withMapper(s -> s == null ? null : BigDecimal.valueOf(Double.parseDouble(s)))
                );

        String argValue = "1234.56789";
        String[] args = {name, argValue};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(BigDecimal.valueOf(1234.56789d));
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnMappedValue_CustomMapper() throws CommandExecutionException {
        // given
        ValueMapper<List<String>> mapper = input -> input == null ? null : Arrays.stream(input.split(",")).map(String::trim).collect(Collectors.toList());

        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ValuedOptionParser
                        .forName(name)
                        .withMapper(mapper)
                );

        String argValue = "one, two, three four, five, six";
        String[] args = {name, argValue};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(List.of("one", "two", "three four", "five", "six"));
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_duplicateNames() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        String expectedValue = "expected value";
        String[] args = {name, expectedValue};
        // when
        Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ValuedOptionParser
                        .forName(name, name))
                .execute(args);

        // then
        TestResult expected = TestResult.newExpected(expectedValue);
        assertEquals(expected, testResult);
    }

    /*

    FAILURE TESTS

     */

    @Test
    public void shouldFail_missingRequiredArgument() {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        ValuedOptionParser<String> requiredParser = ValuedOptionParser
                .forName(name)
                .withRequired(true);
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(requiredParser);
        String[] args = {};

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
        assertEquals(MissingArgumentException.class, actualException.getCause().getClass());
        assertTrue(actualException.getMessage().contains(c.getName()));
        assertTrue(actualException.getMessage().contains(name));
    }

    @Test
    public void shouldFail_unknownArgument() {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ValuedOptionParser.forName(name));
        String unknownArg = "unknown";
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
                .withParsers(ValuedOptionParser
                        .forName(name)
                        .withMapper(throwingMapper));
        String[] args = {name, "anyways"};

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

    @Test
    public void shouldFail_missingValue() {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        ValuedOptionParser<String> stringValuedOptionParser = ValuedOptionParser.forName(name);
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(stringValuedOptionParser);
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
        assertEquals(MissingValueException.class, actualException.getCause().getClass());
        assertTrue(actualException.getMessage().contains(c.getName()));
        assertTrue(actualException.getMessage().contains(name));
    }

}
