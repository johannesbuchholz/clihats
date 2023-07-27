package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.subjects.CliTestInvoker;
import io.github.johannesbuchholz.clihats.processor.subjects.CliTestingNaming;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandAndOptionNamingTest {

    @Test
    public void call_CommandWithoutName() {
        // given
        String[] args = {"method-without-command-name", "-a", "42"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(CliTestingNaming.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("method-without-command-name", "42");
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void callCommandWith_ValuedOptionWithoutName() {
        // given
        String[] args = {"command-with-name", "-v", "69"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(CliTestingNaming.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("command-with-name", "69");
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void callCommandWith_ValuedOptionWithoutName_Alias() {
        // given
        String[] args = {"command-with-name", "--valued-without-name", "69"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(CliTestingNaming.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("command-with-name", "69");
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void callCommandWith_FlagOptionWithoutName() {
        // given
        String[] args = {"command-with-name-2", "-f"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(CliTestingNaming.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("command-with-name-2", "flag-value");
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void callCommandWith_FlagOptionWithoutName_Alias() {
        // given
        String[] args = {"command-with-name-2", "--flag-without-name"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(CliTestingNaming.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("command-with-name-2", "flag-value");
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void call_CommandWithoutName_ValuedOptionWithoutName_AliasFlagOptionWithoutName_Alias() {
        // given
        String[] args = {"all-together", "--flag-without-name", "-v", "42"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(CliTestingNaming.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("all-together", "42", "flag-value");
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

}
