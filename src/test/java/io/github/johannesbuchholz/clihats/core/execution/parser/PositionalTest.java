package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.TestResult;
import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.CommandExecutionException;
import io.github.johannesbuchholz.clihats.core.execution.InvalidInputArgumentException;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class PositionalTest {

    /*

    SUCCESS TESTS

     */

    @Test
    public void shouldExecute_returnInputArg() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(OperandParser.at(0));
        String stringArg = "a string argument";
        String[] args = {stringArg};

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
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(OperandParser.at(0));
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
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(OperandParser.at(0)
                        .withDefault(defaultValue));
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
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(OperandParser.at(0)
                        .withDefault(() -> defaultValue));
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
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(OperandParser.at(0)
                        .withMapper(s -> s == null ? null : BigDecimal.valueOf(Double.parseDouble(s))));

        String argValue = "1234.56789";
        String[] args = {argValue};

        // when
        c.execute(args);

        // then
        double expectedValue = 1234.56789d;
        TestResult expected = TestResult.newExpected(BigDecimal.valueOf(expectedValue));
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnMappedValue_CustomMapper() throws CommandExecutionException {
        // given
        ValueMapper<List<String>> mapper = input -> input == null ? null : Arrays.stream(input.split(",")).map(String::trim).collect(Collectors.toList());

        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(OperandParser.at(0)
                        .withMapper(mapper));

        String argValue = "one, two, three four, five, six";
        String[] args = {argValue};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(List.of("one", "two", "three four", "five", "six"));
        assertEquals(expected, testResult);
    }

    /*

    FAILURE TESTS

     */

    @Test
    public void shouldFail_creatingArgumentWithNegativePosition() {
        // given
        // when
        IllegalArgumentException actualException = null;
        int illegalPosition = -999;
        try {
            Command.forName("run")
                    .withInstruction(args -> {})
                    .withParsers(OperandParser.at(illegalPosition));
        } catch (IllegalArgumentException e) {
            actualException = e;
        }

        // then
        assertNotNull(actualException);
        assertTrue(actualException.getMessage().contains(String.valueOf(illegalPosition)));
    }

    @Test
    public void shouldFail_missingRequiredArgument() {
        // given
        TestResult testResult = TestResult.newEmpty();
        OperandParser<String> requiredParser = OperandParser.at(0)
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
        assertTrue(actualException.getMessage().contains("0"));
    }

    @Test
    public void shouldFail_missingSecondPositionalParser() {
        // given
        TestResult testResult = TestResult.newEmpty();
        OperandParser<String> requiredParser1 = OperandParser.at(0).withRequired(true);
        OperandParser<String> requiredParser2 = OperandParser.at(1).withRequired(true);
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(requiredParser1, requiredParser2);
        String firstArg = "first";
        String[] args = {firstArg};

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
        assertTrue(actualException.getMessage().contains("1"));
    }

    @Test
    public void shouldFail_mappingError() {
        // given
        TestResult testResult = TestResult.newEmpty();
        IllegalArgumentException expectedThrow = new IllegalArgumentException("I am the test exception");
        ValueMapper<Object> throwingMapper = str -> {
            throw expectedThrow;
        };
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(OperandParser.at(0)
                        .withMapper(throwingMapper));
        String[] args = {"anyways"};

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
        assertTrue(actualException.getMessage().contains("0"));
    }

}
