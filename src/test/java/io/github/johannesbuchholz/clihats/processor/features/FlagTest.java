package io.github.johannesbuchholz.clihats.processor.features;

import io.github.johannesbuchholz.clihats.processor.GlobalTestResult;
import io.github.johannesbuchholz.clihats.processor.subjects.Cli3;
import io.github.johannesbuchholz.clihats.processor.subjects.CliTestInvoker;
import io.github.johannesbuchholz.clihats.processor.subjects.misc.MyClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FlagTest {

    @Test
    public void testFlag_expectNullAsDefaultValues_andDeclaredDefaultValues() {
        // given
        String[] args = {"flag-parser"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("flag-parser", null, "my-default", null, null, null);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testFlag_expectFlagStringOnFlagWithoutDeclaredDefaultValue() {
        // given
        String[] args = {"flag-parser", "-fnd"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("flag-parser", "value", "my-default", null, null, null);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testFlag_expectDeclaredFlagValue() {
        // given
        String[] args = {"flag-parser", "-fd"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("flag-parser", null, "my-default", "my-value", null, null);
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

    @Test
    public void testFlag_expectMatchOnAliases() {
        // given
        String[][] args = {{"flag-parser", "-fa"}, {"flag-parser", "-fa1"}, {"flag-parser", "-fa2"}, {"flag-parser", "-fa3"}};
        for (String[] argArray : args) {
            // when
            CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, argArray);
            // then
            GlobalTestResult expected = GlobalTestResult.constructSuccess("flag-parser", null, "my-default", null, "value", null);
            assertEquals(expected, GlobalTestResult.waitForResult());
        }
    }

    @Test
    public void testFlag_expectMappedValue() {
        // given
        String[] args = {"flag-parser", "-fm"};
        // when
        CliTestInvoker.testGeneratedCliWithThrows(Cli3.class, args);
        // then
        GlobalTestResult expected = GlobalTestResult.constructSuccess("flag-parser", null, "my-default", null, null, new MyClass("some-value"));
        assertEquals(expected, GlobalTestResult.waitForResult());
    }

}
