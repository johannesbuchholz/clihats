package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.TestResult;
import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.exception.CommandExecutionException;
import io.github.johannesbuchholz.clihats.core.execution.exception.InvalidInputArgumentException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.MissingArgumentException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.ValueMappingException;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class ArrayOperandTest {

    /*

    SUCCESS TESTS

     */

    @Test
    public void shouldExecute_returnInputArg() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ArrayOperandParser.at(0));
        String stringArg1 = "a string argument 1";
        String stringArg2 = "a string argument 2";
        String[] args = {stringArg1, stringArg2};

        // when
        c.execute(args);

        // then
        Object[] expectedArgs = {new String[] {stringArg1, stringArg2}};
        TestResult expected = TestResult.newExpected(expectedArgs);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_firstArrayParserTakesAllArguments_OperandParserTakesNone() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ArrayOperandParser.at(0), OperandParser.at(1));
        String stringArg1 = "a string argument 1";
        String stringArg2 = "a string argument 2";
        String stringArg3 = "a string argument 3";
        String[] args = {stringArg1, stringArg2, stringArg3};

        // when
        c.execute(args);

        // then
        Object[] expectedArgs = {new String[] {stringArg1, stringArg2, stringArg3}, null};
        TestResult expected = TestResult.newExpected(expectedArgs);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_firstArrayParserTakesAllArguments_ArrayOperandParserTakesNone() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ArrayOperandParser.at(0), ArrayOperandParser.at(1));
        String stringArg1 = "a string argument 1";
        String stringArg2 = "a string argument 2";
        String stringArg3 = "a string argument 3";
        String[] args = {stringArg1, stringArg2, stringArg3};

        // when
        c.execute(args);

        // then
        Object[] expectedArgs = {new String[] {stringArg1, stringArg2, stringArg3}, null};
        TestResult expected = TestResult.newExpected(expectedArgs);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnDefaultValueNull_whenMissing() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ArrayOperandParser.at(0));
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
        String[] defaultValue = {"a string argument"};
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ArrayOperandParser.at(0)
                        .withDefault(defaultValue));
        String[] args = {};

        // when
        c.execute(args);

        // then
        Object[] expectedArgs = {defaultValue};
        TestResult expected = TestResult.newExpected(expectedArgs);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnCustomDefaultSupplierValue_whenMissing() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String[] defaultValue = {"a string argument"};
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ArrayOperandParser.at(0)
                        .withDefault(() -> defaultValue));
        String[] args = {};

        // when
        c.execute(args);

        // then
        Object[] expectedArgs = {defaultValue};
        TestResult expected = TestResult.newExpected(expectedArgs);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnMappedValue_BigDecimal() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ArrayOperandParser.at(0)
                        .withMapper(s -> s == null ? null : BigDecimal.valueOf(Double.parseDouble(s)), BigDecimal.class));

        String argValue1 = "1234.56789";
        String argValue2 = "7777.77777";
        String[] args = {argValue1, argValue2};

        // when
        c.execute(args);

        // then
        BigDecimal expectedValue1 = BigDecimal.valueOf(1234.56789);
        BigDecimal expectedValue2 = BigDecimal.valueOf(7777.77777);
        Object[] expectedArgs = {new BigDecimal[] {expectedValue1, expectedValue2}};
        TestResult expected = TestResult.newExpected(expectedArgs);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnMappedValue_CustomMapperToString() throws CommandExecutionException {
        // given
        ValueMapper<String> mapper = input -> input.trim().toUpperCase();
        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ArrayOperandParser.at(0)
                        .withMapper(mapper, String.class));

        String argValue1 = "one";
        String argValue2 = "two";
        String argValue3 = "three  ";
        String argValue4 = "  four";
        String argValue5 = "five";
        String[] args = {argValue1, argValue2, argValue3, argValue4, argValue5};

        // when
        c.execute(args);

        // then
        Object[] expectedArgs = {new String[] {"ONE", "TWO", "THREE", "FOUR", "FIVE"}};
        TestResult expected = TestResult.newExpected(expectedArgs);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_returnMappedValue_CustomMapperToPath() throws CommandExecutionException {
        // given
        ValueMapper<Path> mapper = Path::of;
        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ArrayOperandParser.at(0)
                        .withMapper(mapper, Path.class));

        String argValue1 = "a/b/c";
        String argValue2 = "/x/y/z";
        String argValue3 = "/here/and/there.java";
        String[] args = {argValue1, argValue2, argValue3};

        // when
        c.execute(args);

        // then
        Object[] expectedArgs = {new Path[] {Path.of(argValue1), Path.of(argValue2), Path.of(argValue3)}};
        TestResult expected = TestResult.newExpected(expectedArgs);
        assertEquals(expected, testResult);
    }

    /*

    FAILURE TESTS

     */

    @Test
    public void shouldFail_creatingArgumentWithNegativePosition() {
        // given
        // when
        int illegalPosition = -999;
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class, () ->
                Command.forName("run")
                        .withInstruction(args -> {})
                        .withParsers(ArrayOperandParser.at(illegalPosition)));
        // then
        assertTrue(actualException.getMessage().contains(String.valueOf(illegalPosition)));
    }

    @Test
    public void shouldFail_missingRequiredArgument() {
        // given
        TestResult testResult = TestResult.newEmpty();
        int index = 0;
        ArrayOperandParser<String> requiredParser = ArrayOperandParser.at(index)
                .withRequired(true);
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(requiredParser);
        String[] args = {};

        // when
        // then
        CommandExecutionException actualException = assertThrows(CommandExecutionException.class, () -> c.execute(args));
        assertEquals(InvalidInputArgumentException.class, actualException.getClass());
        assertEquals(MissingArgumentException.class, actualException.getCause().getClass());
        assertTrue(actualException.getMessage().contains(c.getName()));
        assertTrue(actualException.getMessage().contains(String.valueOf(index)));
    }

    @Test
    public void shouldFail_requiredArgumentMissesAlthoughDefaultValueIsSet() {
        // given
        TestResult testResult = TestResult.newEmpty();
        int index = 0;
        String[] defaultValue = {"default-value"};
        String[] args = {};
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ArrayOperandParser.at(index)
                        .withRequired(true)
                        .withDefault(defaultValue));

        // when
        // then
        CommandExecutionException actualException = assertThrows(CommandExecutionException.class, () -> c.execute(args));
        assertEquals(InvalidInputArgumentException.class, actualException.getClass());
        assertEquals(MissingArgumentException.class, actualException.getCause().getClass());
        assertTrue(actualException.getMessage().contains(c.getName()));
        assertTrue(actualException.getMessage().contains(String.valueOf(index)));
    }

    @Test
    public void shouldFail_requiredArgumentMissesAlthoughDefaultSupplierValueIsSet() {
        // given
        TestResult testResult = TestResult.newEmpty();
        int index = 0;
        String[] defaultValue = {"default-value-one", "default-value-two"};
        String[] args = {};
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ArrayOperandParser.at(index)
                        .withRequired(true)
                        .withDefault(() -> defaultValue));

        // when
        // then
        CommandExecutionException actualException = assertThrows(CommandExecutionException.class, () -> c.execute(args));
        assertEquals(InvalidInputArgumentException.class, actualException.getClass());
        assertEquals(MissingArgumentException.class, actualException.getCause().getClass());
        assertTrue(actualException.getMessage().contains(c.getName()));
        assertTrue(actualException.getMessage().contains(String.valueOf(index)));
    }

    @Test
    public void shouldFail_missingSecondArrayOperandParser() {
        // given
        TestResult testResult = TestResult.newEmpty();
        ArrayOperandParser<String> requiredParser1 = ArrayOperandParser.at(0).withRequired(true);
        ArrayOperandParser<String> requiredParser2 = ArrayOperandParser.at(1).withRequired(true);
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(requiredParser1, requiredParser2);
        String firstArg = "first";
        String[] args = {firstArg};

        // when
        // then
        CommandExecutionException actualException = assertThrows(CommandExecutionException.class, () -> c.execute(args));
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
                .withParsers(ArrayOperandParser.at(0)
                        .withMapper(throwingMapper, Object.class));
        String[] args = {"anyways"};

        // when
        // then
        CommandExecutionException actualException = assertThrows(CommandExecutionException.class, () -> c.execute(args));
        assertEquals(InvalidInputArgumentException.class, actualException.getClass());
        assertEquals(ValueMappingException.class, actualException.getCause().getClass());
        assertTrue(actualException.getMessage().contains(c.getName()));
        assertTrue(actualException.getMessage().contains("0"));
    }

}
