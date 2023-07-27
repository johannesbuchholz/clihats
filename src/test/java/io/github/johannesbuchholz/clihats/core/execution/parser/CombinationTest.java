package io.github.johannesbuchholz.clihats.core.execution.parser;

import io.github.johannesbuchholz.clihats.core.TestResult;
import io.github.johannesbuchholz.clihats.core.exceptions.execution.CommandExecutionException;
import io.github.johannesbuchholz.clihats.core.execution.Command;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
                        ValuedParser.forName(nameA),
                        ValuedParser.forName(nameB)
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
                        ValuedParser.forName(nameA),
                        ValuedParser.forName(nameB)
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
                        ValuedParser.forName(nameA),
                        ValuedParser.forName(nameB)
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
                        ValuedParser.forName(nameA),
                        ValuedParser.forName(nameB)
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
                        FlagParser.forName(nameA),
                        FlagParser.forName(nameB)
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
                        FlagParser.forName(nameA),
                        FlagParser.forName(nameB)
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
                        FlagParser.forName(nameA),
                        FlagParser.forName(nameB)
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
                        FlagParser.forName(nameA),
                        FlagParser.forName(nameB)
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
                        PositionalParser.at(0),
                        PositionalParser.at(1)
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
                        PositionalParser.at(0),
                        PositionalParser.at(1)
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
                        PositionalParser.at(0),
                        PositionalParser.at(1)
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
                        ValuedParser.forName(nameA),
                        FlagParser.forName(nameB)
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
                        ValuedParser.forName(nameA),
                        FlagParser.forName(nameB)
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
                        ValuedParser.forName(nameA),
                        FlagParser.forName(nameB)
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
                        ValuedParser.forName(nameA),
                        FlagParser.forName(nameB)
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
                        ValuedParser.forName(nameA),
                        PositionalParser.at(0)
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
                        ValuedParser.forName(nameA),
                        PositionalParser.at(0)
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
                        ValuedParser.forName(nameA),
                        PositionalParser.at(0)
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
                        ValuedParser.forName(nameA),
                        PositionalParser.at(0)
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
                        FlagParser.forName(nameA),
                        PositionalParser.at(0)
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
                        FlagParser.forName(nameA),
                        PositionalParser.at(0)
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
                        FlagParser.forName(nameA),
                        PositionalParser.at(0)
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
                        FlagParser.forName(nameA),
                        PositionalParser.at(0)
                );
        String stringArgA = "a string arg A";
        String[] args = {stringArgA, nameA};

        // when
        c.execute(args);

        // then
        TestResult expected = TestResult.newExpected("", stringArgA);
        assertEquals(expected, testResult);
    }
    
}
