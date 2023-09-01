package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.TestResult;
import io.github.johannesbuchholz.clihats.core.exceptions.execution.CommandExecutionException;
import io.github.johannesbuchholz.clihats.core.exceptions.execution.ParsingException;
import io.github.johannesbuchholz.clihats.core.exceptions.parsing.MissingValueException;
import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.ParsingResult;
import io.github.johannesbuchholz.clihats.core.execution.ValueMapper;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
                .withParsers(ValuedParser.forName(name));
        String stringArg = "a string argument";
        String[] args = {name, stringArg};

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
                .withParsers(ValuedParser
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
                .withParsers(ValuedParser.forName(name));
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
                .withParsers(ValuedParser
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
                .withParsers(ValuedParser
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
                .withParsers(ValuedParser
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
                .withParsers(ValuedParser
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
                .withParsers(ValuedParser
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
        ValuedParser<String> requiredParser = ValuedParser
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

        // and expected
        ParsingResult.Builder parsingResultBuilder = ParsingResult.builder(1);
        parsingResultBuilder.putMissing(requiredParser);
        String expectedMessage = new ParsingException(c, parsingResultBuilder.build()).getMessage();


        // then
        assertNotNull(actualException);
        assertEquals(ParsingException.class, actualException.getClass());
        assertEquals(expectedMessage, actualException.getMessage());
    }

    @Test
    public void shouldFail_unknownArgument() {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(ValuedParser.forName(name));
        String unknownArg = "unknown";
        String[] args = {unknownArg};

        // when
        CommandExecutionException actualException = null;
        try {
            c.execute(args);
        } catch (CommandExecutionException e) {
            actualException = e;
        }

        // and expected
        ParsingResult.Builder parsingResultBuilder = ParsingResult.builder(1);
        parsingResultBuilder.putUnknown(unknownArg);
        String expectedMessage = new ParsingException(c, parsingResultBuilder.build()).getMessage();

        // then
        assertNotNull(actualException);
        assertEquals(ParsingException.class, actualException.getClass());
        assertEquals(expectedMessage, actualException.getMessage());
    }

    @Test
    public void shouldFail_unknownArgumentAndRequiredArgumentMissing() {
        // given
        TestResult testResult = TestResult.newEmpty();
        ValuedParser<String> requiredParser = ValuedParser.forName("-a").withRequired(true);
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(requiredParser);
        String unknownArg = "unknown";
        String[] args = {unknownArg};

        // when
        CommandExecutionException actualException = null;
        try {
            c.execute(args);
        } catch (CommandExecutionException e) {
            actualException = e;
        }

        // and expected
        ParsingResult.Builder parsingResultBuilder = ParsingResult.builder(1);
        parsingResultBuilder.putUnknown(unknownArg);
        parsingResultBuilder.putMissing(requiredParser);
        String expectedMessage = new ParsingException(c, parsingResultBuilder.build()).getMessage();

        // then
        assertNotNull(actualException);
        assertEquals(ParsingException.class, actualException.getClass());
        assertEquals(expectedMessage, actualException.getMessage());
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
                .withParsers(ValuedParser
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

        // and expected
        ParsingResult.Builder parsingResultBuilder = ParsingResult.builder(1);
        parsingResultBuilder.putError(expectedThrow);
        String expectedMessage = new ParsingException(c, parsingResultBuilder.build()).getMessage();

        // then
        assertNotNull(actualException);
        assertEquals(ParsingException.class, actualException.getClass());
        assertEquals(expectedMessage, actualException.getMessage());
    }

    @Test
    public void shouldFail_missingValue() {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-a";
        ValuedParser<String> stringValuedParser = ValuedParser.forName(name);
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(stringValuedParser);
        String[] args = {name};

        // when
        CommandExecutionException actualException = null;
        try {
            c.execute(args);
        } catch (CommandExecutionException e) {
            actualException = e;
        }

        // and expected
        ParsingResult.Builder parsingResultBuilder = ParsingResult.builder(1);
        parsingResultBuilder.putError(new MissingValueException(stringValuedParser));
        String expectedMessage = new ParsingException(c, parsingResultBuilder.build()).getMessage();

        // then
        assertNotNull(actualException);
        assertEquals(ParsingException.class, actualException.getClass());
        assertEquals(expectedMessage, actualException.getMessage());
    }

}
