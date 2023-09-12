package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.TestResult;
import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.exception.CommandExecutionException;
import io.github.johannesbuchholz.clihats.core.execution.exception.InvalidInputArgumentException;
import io.github.johannesbuchholz.clihats.core.execution.parser.exception.UnknownArgumentException;
import org.junit.Test;

import static org.junit.Assert.*;

public class CombinationTest {

    /*

    VALUE VALUE TEST

     */

    @Test
    public void shouldExecute_ValueValue_takeFirst() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        ValuedOptionParser.forName(nameB)
                );
        String stringArg = "a string argument";
        String[] args = {nameA, stringArg};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArg, null);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_ValueValue_takeSecond() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        ValuedOptionParser.forName(nameB)
                );
        String stringArg = "a string argument";
        String[] args = {nameB, stringArg};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(null, stringArg);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_ValueValue_takeBoth_resultsIn_A_B() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        ValuedOptionParser.forName(nameB)
                );
        String stringArgA = "a string argument A";
        String stringArgB = "a string argument B";
        String[] args = {nameA, stringArgA, nameB, stringArgB};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArgA, stringArgB);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_ValueValue_takeBoth_inverseOrder_resultsIn_A_B() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        ValuedOptionParser.forName(nameB)
                );
        String stringArgA = "a string argument A";
        String stringArgB = "a string argument B";
        String[] args = {nameB, stringArgB, nameA, stringArgA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArgA, stringArgB);
        assertEquals(expected, testResult);
    }

    /*

    FLAG FLAG TEST

     */

    @Test
    public void shouldExecute_FlagFlag_takeFirst() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        FlagOptionParser.forName(nameA),
                        FlagOptionParser.forName(nameB)
                );
        String[] args = {nameA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected("", null);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_FlagFlag_takeSecond() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        FlagOptionParser.forName(nameA),
                        FlagOptionParser.forName(nameB)
                );
        String[] args = {nameB};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(null, "");
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_FlagFlag_takeBoth_resultsIn_A_B() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        FlagOptionParser.forName(nameA),
                        FlagOptionParser.forName(nameB)
                );
        String[] args = {nameA, nameB};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected("", "");
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_FlagFlag_takeBoth_inverseOrder_resultsIn_A_B() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        FlagOptionParser.forName(nameA),
                        FlagOptionParser.forName(nameB)
                );
        String[] args = {nameB, nameA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected("", "");
        assertEquals(expected, testResult);
    }

    /*

    POS POS TEST

     */

    @Test
    public void shouldExecute_PosPos_takeFirst() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        OperandParser.at(0),
                        OperandParser.at(1)
                );
        String stringArgA = "a string arg A";
        String[] args = {stringArgA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArgA, null);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_PosPos_takeBoth_resultsIn_A_B() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        OperandParser.at(0),
                        OperandParser.at(1)
                );
        String stringArgA = "a string arg A";
        String stringArgB = "a string arg B";
        String[] args = {stringArgA, stringArgB};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArgA, stringArgB);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_PosPos_takeBoth_inverseOrder_resultsIn_B_A() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        OperandParser.at(0),
                        OperandParser.at(1)
                );
        String stringArgA = "a string arg A";
        String stringArgB = "a string arg B";
        String[] args = {stringArgB, stringArgA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArgB, stringArgA);
        assertEquals(expected, testResult);
    }

    /*

    VALUE FLAG TEST

     */

    @Test
    public void shouldExecute_ValueFlag_takeName() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        FlagOptionParser.forName(nameB)
                );
        String stringArgA = "a string arg A";
        String[] args = {nameA, stringArgA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArgA, null);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_ValueFlag_takeFlag() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        FlagOptionParser.forName(nameB)
                );
        String[] args = {nameB};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(null, "");
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_ValueFlag_takeBoth_resultsIn_A_B() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        FlagOptionParser.forName(nameB)
                );
        String stringArgA = "a string arg A";
        String[] args = {nameA, stringArgA, nameB};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArgA, "");
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_ValueFlag_takeBoth_inverseOrder_resultsIn_A_B() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        String nameB = "-b";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        FlagOptionParser.forName(nameB)
                );
        String stringArgA = "a string arg A";
        String[] args = {nameB, nameA, stringArgA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArgA, "");
        assertEquals(expected, testResult);
    }

    /*

    VALUE POS TEST

     */

    @Test
    public void shouldExecute_ValuePos_takeName() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        OperandParser.at(0)
                );
        String stringArgA = "a string arg A";
        String[] args = {nameA, stringArgA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArgA, null);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_ValuePos_takePos() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        OperandParser.at(0)
                );
        String stringArgB = "a string arg B";
        String[] args = {stringArgB};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(null, stringArgB);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_ValuePos_takeBoth_resultsIn_A_B() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        OperandParser.at(0)
                );
        String stringArgA = "a string arg A";
        String stringArgB = "a string arg B";
        String[] args = {nameA, stringArgA, stringArgB};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArgA, stringArgB);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_ValuePos_takeBoth_inverseOrder_resultsIn_A_B() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(nameA),
                        OperandParser.at(0)
                );
        String stringArgA = "a string arg A";
        String stringArgB = "a string arg B";
        String[] args = {stringArgB, nameA, stringArgA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(stringArgA, stringArgB);
        assertEquals(expected, testResult);
    }


    /*

    FLAG POS TEST

     */

    @Test
    public void shouldExecute_FlagPos_takeFlag() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        FlagOptionParser.forName(nameA),
                        OperandParser.at(0)
                );
        String[] args = {nameA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected("", null);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_FlagPos_takePos() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        FlagOptionParser.forName(nameA),
                        OperandParser.at(0)
                );
        String stringArgA = "a string arg A";
        String[] args = {stringArgA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected(null, stringArgA);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_FlagPos_takeBoth_resultsIn_A_B() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        FlagOptionParser.forName(nameA),
                        OperandParser.at(0)
                );
        String stringArgA = "a string arg A";
        String[] args = {nameA, stringArgA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected("", stringArgA);
        assertEquals(expected, testResult);
    }

    @Test
    public void shouldExecute_FlagPos_takeBoth_inverseOrder_resultsIn_A_B() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        FlagOptionParser.forName(nameA),
                        OperandParser.at(0)
                );
        String stringArgA = "a string arg A";
        String[] args = {stringArgA, nameA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected("", stringArgA);
        assertEquals(expected, testResult);
    }

    /*

    MIXED

     */

    @Test
    public void shouldExecute_giveFlagFlagPos_takePosAndFlagOnce_resultsIn_unknownArgument() {
        // given
        String nameA = "-a";
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(
                        FlagOptionParser.forName(nameA),
                        OperandParser.at(0)
                );
        String stringArgA = "a string arg A";
        String[] args = {stringArgA, nameA, nameA};

        // when
        // then
        InvalidInputArgumentException commandExecutionException = assertThrows(InvalidInputArgumentException.class, () -> c.execute(args));
        Throwable cause = commandExecutionException.getCause();

        assertNotNull(cause);
        assertEquals(cause.getClass(), UnknownArgumentException.class);
        assertTrue(cause.getMessage().contains(nameA));
    }

    @Test
    public void shouldExecute_giveFlagFlagValue_takeValueAndFlagOnce_resultsIn_unknownArgument() {
        // given
        String nameA = "-a";
        Command c = Command.forName("run")
                .withInstruction(args -> {})
                .withParsers(
                        FlagOptionParser.forName(nameA),
                        ValuedOptionParser.forName("-v")
                );
        String[] args = {"-aav", "some value"};

        // when
        // then
        InvalidInputArgumentException commandExecutionException = assertThrows(InvalidInputArgumentException.class, () -> c.execute(args));
        Throwable cause = commandExecutionException.getCause();

        assertNotNull(cause);
        assertEquals(cause.getClass(), UnknownArgumentException.class);
        assertTrue(cause.getMessage().contains(nameA));
    }

    @Test
    public void shouldExecute_giveFlagFlag_takeFlagOnce_resultsIn_secondFlagInputArgumentTakenAsOperandValue() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String nameA = "-a";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        FlagOptionParser.forName(nameA),
                        OperandParser.at(0)
                );
        String[] args = {"-aa"};

        // when
        c.execute(args);

        // then
        TestResult expectedResult = TestResult.newExpected("", "-a");
        assertEquals(expectedResult, testResult);
    }

    @Test
    public void shouldExecute_givenValueValue_takeValueOnce_resultsIn_secondValueInputArgumentTakenAsOperandValue() throws CommandExecutionException {
        // given
        TestResult testResult = TestResult.newEmpty();
        String name = "-v";
        Command c = Command.forName("run")
                .withInstruction(testResult.getTestInstruction())
                .withParsers(
                        ValuedOptionParser.forName(name),
                        OperandParser.at(0)
                );
        String[] args = {"-vv", "option-value"};

        // when
        c.execute(args);

        // then
        TestResult expectedResult = TestResult.newExpected("option-value", "-v");
        assertEquals(expectedResult, testResult);
    }

}
