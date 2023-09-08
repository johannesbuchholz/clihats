package io.github.johannesbuchholz.clihats.core;

import io.github.johannesbuchholz.clihats.core.execution.CliException;
import io.github.johannesbuchholz.clihats.core.execution.Command;
import io.github.johannesbuchholz.clihats.core.execution.Commander;
import io.github.johannesbuchholz.clihats.core.execution.exception.*;
import io.github.johannesbuchholz.clihats.core.execution.parser.AbstractArgumentParser;
import io.github.johannesbuchholz.clihats.core.execution.parser.ArgumentParsers;
import io.github.johannesbuchholz.clihats.processor.mapper.defaults.BooleanMapper;
import io.github.johannesbuchholz.clihats.processor.mapper.defaults.LocalDateMapper;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Depends on sequential execution of tests. Order does not matter.
 */
public class CommanderTest {

    public static void dummyAdapter1(Object[] args) {
        dummyTestMethod1((X) args[0], (String) args[1]);
    }

    public static void dummyAdapter2(Object[] args) {
        dummyTestMethod2((X) args[0], (String) args[1]);
    }

    public static void dummyTestMethod1(X arg1, String arg2) {
        result = new R("1: " + String.join(", ", arg1.toString(), arg2));
    }

    public static void dummyTestMethod2(X arg1, String arg2) {
        result = new R("2: " + String.join(", ", arg1.toString(), arg2));
    }

    public static void dummyMethod3(String arg, String s, Boolean aBoolean, LocalDate localDate) {
        result = new R("3: " + String.join(", ", arg, s, aBoolean.toString(), localDate.toString()));
    }

    public static class X {
        private final String s;

        public X(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return "X{" +
                    "s=" + s +
                    '}';
        }
    }

    public static class R {
        private final String r;

        public R(String r) {
            this.r = r;
        }

        @Override
        public String toString() {
            return ">>>>> Result:\n" + r;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            R r1 = (R) o;
            return Objects.equals(r, r1.r);
        }

        @Override
        public int hashCode() {
            return Objects.hash(r);
        }
    }

    private static R result;

    private Command c2;
    private Commander commander1;
    private Commander commander2;

    @Before
    public void setup() {
        result = null;

        AbstractArgumentParser<?> p1 = ArgumentParsers.operand(0)
                .withMapper(X::new);
        AbstractArgumentParser<?> p2 = ArgumentParsers.valuedOption("-d")
                .withDefault("999.99");
        Command c1 = Command.forName("execute-first")
                .withInstruction(CommanderTest::dummyAdapter1)
                .withParsers(p1, p2);

        c2 = Command.forName("execute-second").withInstruction(CommanderTest::dummyAdapter2)
                .withParsers(p1, p2);

        Command c3 = Command.forName("run-runner")
                .withInstruction(args -> result = new R("run-runner-blubb"))
                .withDescription("Command without args");

        commander1 = Commander.forName("commander1")
                .withCommands(c1, c2, c3)
                .withDescription("This is the first commander in this test class. We try to add as much info to this description text as possible.");

        commander2 = Commander.forName("my-second-cli")
                .withDescription("some other class for executing useless commands")
                .withCommands(
                        Command.forName("print-all").withInstruction(args -> dummyMethod3((String) args[0], (String) args[1], (Boolean) args[2], (LocalDate) args[3]))
                                .withDescription("prints all input arguments to console")
                                .withParsers(
                                        ArgumentParsers.operand(0),
                                        ArgumentParsers.operand(1),
                                        ArgumentParsers.flagOption("-f", "--flag").withFlagValue("true").withMapper(new BooleanMapper()),
                                        ArgumentParsers.valuedOption("-t", "--time").withMapper(new LocalDateMapper())
                                ));
    }

    @Test
    public void c1ShouldExecute() throws CommanderExecutionException, CliHelpCallException {
        String[] args = {"execute-first", "first arg", "-d", "no-stupid-questions"};
        dummyTestMethod1(new X("first arg"), "no-stupid-questions");
        R expected = result;

        commander1.execute(args);

        assertEquals(expected, result);
    }

    @Test
    public void c2ShouldExecute() throws CommanderExecutionException, CliHelpCallException {
        String[] args = {"execute-second", "first arg", "-d", "no-stupid-questions"};
        dummyTestMethod2(new X("first arg"), "no-stupid-questions");
        R expected = result;

        commander1.execute(args);

        assertEquals(expected, result);
    }

    @Test(expected = CommanderCreationException.class)
    public void commanderCreationShouldFail_sameCommandName() {
        AbstractArgumentParser<?> p1 = ArgumentParsers.operand(0)
                .withMapper(X::new);
        AbstractArgumentParser<?> p2 = ArgumentParsers.valuedOption("-d")
                .withDefault("999.99");
        Command c3 = Command.forName("execute-second").withInstruction(CommanderTest::dummyAdapter2).withParsers(p1, p2);
        Commander.forName("commander2")
                .withCommands(c2, c3);
    }

    @Test
    public void commander_unknownCommand() {
        String[] args = {"execute", "unknown", "first arg", "-d=no-stupid-questions"};
        Throwable t = null;
        try {
            commander1.execute(args);
        } catch (CliException e) {
            t = e;
        }
        assertNotNull(t);
        assertEquals(UnknownCommandException.class, t.getClass());
    }

    @Test
    public void commander_parsingException() {
        String[] args = {"execute-first", "fubbelmug-wojk", "-ddd=no-stupid-questions", "fourth-unknown-arg"};
        Throwable t = null;
        try {
            commander1.execute(args);
        } catch (CliException e) {
            t = e;
        }
        assertNotNull(t);
        assertEquals(InvalidInputArgumentException.class, t.getCause().getClass());
    }

    @Test
    public void commander_emptyArgs() throws CommanderExecutionException, CliHelpCallException {
        String[] args = {"run-runner"};
        commander1.execute(args);
        assertEquals(new R("run-runner-blubb"), result);
    }

    @Test
    public void runCommander2_shouldExecute() throws CommanderExecutionException, CliHelpCallException {
        String[] args = {"print-all", "12323876567823dfgshfghsd", "jhsjhgsjhfg", "-f", "--time", "2222-12-22"};
        commander2.execute(args);
        assertEquals(
                new R("3: " + String.join(", ", "12323876567823dfgshfghsd", "jhsjhgsjhfg", Boolean.TRUE.toString(), LocalDate.parse("2222-12-22").toString())),
                result
        );
    }


}
