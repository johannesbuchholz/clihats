package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.ReusableTestResult;
import io.github.johannesbuchholz.clihats.processor.annotations.Argument;
import io.github.johannesbuchholz.clihats.processor.annotations.Command;
import io.github.johannesbuchholz.clihats.processor.annotations.CommandLineInterface;
import io.github.johannesbuchholz.clihats.processor.execution.CliHats;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@CommandLineInterface
public class CommandAndArgumentNamingTest {

    @Command(cli = CommandAndArgumentNamingTest.class)
    public static void methodWithoutCommandName(
            @Argument(name = "-a") String a
    ) {
       result.put("method-without-command-name", a);
    }

    @Command(name = "command-with-name", cli = CommandAndArgumentNamingTest.class)
    public static void methodWithName(
            @Argument String valuedWithoutName
    ) {
       result.put("command-with-name", valuedWithoutName);
    }

    @Command(name = "command-with-name-2", cli = CommandAndArgumentNamingTest.class)
    public static void methodWithName2(
            @Argument(flagValue = "flag-value") String flagWithoutName
    ) {
       result.put("command-with-name-2", flagWithoutName);
    }

    @Command(cli = CommandAndArgumentNamingTest.class)
    public static void allTogether(
            @Argument String valuedWithoutName,
            @Argument(flagValue = "flag-value") String flagWithoutName
    ) {
       result.put("all-together", valuedWithoutName, flagWithoutName);
    }

    private static final ReusableTestResult result = new ReusableTestResult();

    @Before
    public void setup() {
        result.clear();
    }

    @Test
    public void call_CommandWithoutName() {
        // given
        String[] args = {"method-without-command-name", "-a", "42"};
        // when
        CliHats.get(CommandAndArgumentNamingTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("method-without-command-name", "42");
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void callCommandWith_ValuedOptionWithoutName() {
        // given
        String[] args = {"command-with-name", "-v", "69"};
        // when
        CliHats.get(CommandAndArgumentNamingTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("command-with-name", "69");
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void callCommandWith_ValuedOptionWithoutName_Alias() {
        // given
        String[] args = {"command-with-name", "--valued-without-name", "69"};
        // when
        CliHats.get(CommandAndArgumentNamingTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("command-with-name", "69");
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void callCommandWith_FlagOptionWithoutName() {
        // given
        String[] args = {"command-with-name-2", "-f"};
        // when
        CliHats.get(CommandAndArgumentNamingTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("command-with-name-2", "flag-value");
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void callCommandWith_FlagOptionWithoutName_Alias() {
        // given
        String[] args = {"command-with-name-2", "--flag-without-name"};
        // when
        CliHats.get(CommandAndArgumentNamingTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("command-with-name-2", "flag-value");
        assertEquals(expected, result.getAndClear());
    }

    @Test
    public void call_CommandWithoutName_ValuedOptionWithoutName_AliasFlagOptionWithoutName_Alias() {
        // given
        String[] args = {"all-together", "--flag-without-name", "-v", "42"};
        // when
        CliHats.get(CommandAndArgumentNamingTest.class).execute(args);
        // then
        ReusableTestResult.Result expected = ReusableTestResult.getExpected("all-together", "42", "flag-value");
        assertEquals(expected, result.getAndClear());
    }

}
